/*
 * Copyright 2018-2021 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.ena.webin.cli.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.client.model.auth.AuthRealm;
import uk.ac.ebi.biosamples.client.service.WebinAuthClientService;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceException;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceMessage;
import uk.ac.ebi.ena.webin.cli.utils.RetryUtils;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class 
SampleService extends WebinService
{
    public static final String SERVICE_NAME = "Sample";

    public static final String BIOSAMPLES_ID_PREFIX = "SAM";

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleService.class);

    private final String biosamplesWebinUserName;
    private final String biosamplesWebinPassword;

    private final WebinAuthClientService webinAuthClientService;

    private final BiosamplesService biosamplesService;

    private final SampleXmlService sampleXmlService;

    protected 
    SampleService( Builder builder )
    {
        super( builder );

        this.biosamplesWebinUserName = builder.biosamplesWebinUserName;
        this.biosamplesWebinPassword = builder.biosamplesWebinPassword;

        webinAuthClientService = createWebinAuthClientService();

        biosamplesService = new BiosamplesService();

        sampleXmlService = new SampleXmlService.Builder()
            .setAuthToken(getAuthToken())
            .setUserName(getUserName())
            .setPassword(getPassword())
            .setTest(getTest())
            .build();
    }

    public static class
    Builder extends AbstractBuilder<SampleService> {
        protected String biosamplesWebinUserName;
        protected String biosamplesWebinPassword;

        @Override
        public Builder setUserName(String userName) {
            super.setUserName(userName);
            return this;
        }

        @Override
        public Builder setPassword(String password) {
            super.setPassword(password);
            return this;
        }

        @Override
        public Builder setCredentials(String userName, String password) {
            super.setCredentials(userName, password);
            return this;
        }

        @Override
        public Builder setTest(boolean test) {
            super.setTest(test);
            return this;
        }

        @Override
        public Builder setAuthToken(String authToken) {
            super.setAuthToken(authToken);
            return this;
        }

        public Builder setBiosamplesWebinUserName(String biosamplesWebinUserName) {
            this.biosamplesWebinUserName = biosamplesWebinUserName;
            return this;
        }

        public Builder setBiosamplesWebinPassword(String biosamplesWebinPassword) {
            this.biosamplesWebinPassword = biosamplesWebinPassword;
            return this;
        }

        @Override
        public SampleService
        build() {
            return new SampleService(this);
        }
    }

    /**
     * Depending on what the given sample ID looks like, sample information will be retrieved from either ENA or Biosamples.
     * Also, if the sample is not retrieved from Biosamples then an attempt will be made to retrieve it from ENA.
     */
    public Sample getSample(String sampleId) {
        if (isBiosamplesId(sampleId)) {
            Sample biosamplesSample = getBiosamplesSample(sampleId);
            if (isBiosamplesSampleValid(biosamplesSample)) {
                return biosamplesSample;
            }
        }

        // If the sample couldn't be retrieved from Biosamples above then retrieve it from ENA.
        Sample sraSample = getSraSample(sampleId);
        if (sraSample == null) {
            return null;
        }

        // If SRA sample has a Biosamples accession then retrieve it from Biosamples using this accession. This is
        // becuase getting samples data from Biosamples is always preferred.
        if (sraSample.getBioSampleId() != null) {
            Sample biosamplesSample = getBiosamplesSample(sraSample.getBioSampleId());
            if (isBiosamplesSampleValid(biosamplesSample)) {
                return biosamplesSample;
            }
        }

        // Getting here means we couldn't get sample from Biosamples. So return the SRA sample instead after adding
        // attribute information to it.
        Sample sampleFromXml = sampleXmlService.getSample(sraSample.getSraSampleId());
        sraSample.setAttributes(sampleFromXml.getAttributes());

        return sraSample;
    }

    public static boolean isBiosamplesId(String biosampleId) {
        return biosampleId.toUpperCase().startsWith(BIOSAMPLES_ID_PREFIX);
    }

    /**
     * Returned sample will not have attribute information.
     */
    private Sample getSraSample(String sampleId) {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<SampleResponse> response = executeHttpGet( restTemplate ,  getAuthHeader(),  sampleId,  getTest());

        SampleResponse sampleResponse = response.getBody();
        if (sampleResponse == null || !sampleResponse.canBeReferenced) {
            throw new ServiceException(ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleId));
        }

        Sample sample = new Sample();
        sample.setBioSampleId(sampleResponse.bioSampleId);
        sample.setTaxId(sampleResponse.taxId);
        sample.setOrganism(sampleResponse.organism);
        sample.setSraSampleId(sampleResponse.id);

        return sample;
    }

    /**
     * Returned sample will contain attribute information as well.
     */
    private Sample getBiosamplesSample(String sampleId) {
        uk.ac.ebi.biosamples.model.Sample biosamplesSample = biosamplesService.findSampleById(
            sampleId, getBiosamplesAuthToken());
        if (biosamplesSample == null) {
            return null;
        }

        Sample sample = new Sample();
        sample.setBioSampleId(biosamplesSample.getAccession());
        sample.setTaxId(biosamplesSample.getTaxId() == null ? null : biosamplesSample.getTaxId().intValue());
        sample.setOrganism(biosamplesSample.getAttributes().stream()
            .filter(attr -> attr.getType().equals("organism"))
            .findFirst().map(attr -> attr.getValue())
            .orElse(null));

        List<Attribute> attributes = new ArrayList<>();
        biosamplesSample.getAttributes().forEach(biosamplesAttribute -> {
            String type = biosamplesAttribute.getType();
            if (type != null && !type.isEmpty())
                attributes.add(new Attribute(type, biosamplesAttribute.getValue()));
        });

        sample.setAttributes(attributes);

        return sample;
    }

    /**
     * @return false if given Biosample sample is either null or has incomplete information i.e. Tax ID. True otherwise.
     */
    private boolean isBiosamplesSampleValid(Sample sample) {
        if (sample == null || sample.getTaxId() == null) {
            return false;
        }

        return true;
    }

    private WebinAuthClientService createWebinAuthClientService() {
        String authUrl = getTest() ? WEBIN_AUTH_TEST_URL : WEBIN_AUTH_PROD_URL;

        return new WebinAuthClientService(
            new RestTemplateBuilder(),
            URI.create(authUrl),
            biosamplesWebinUserName,
            biosamplesWebinPassword,
            Arrays.asList(AuthRealm.ENA));
    }

    private String getBiosamplesAuthToken() {
        return RetryUtils.executeWithRetry(
            context -> webinAuthClientService.getJwt(),
            context -> LOGGER.warn("Retrying acquiring authentication token for Biosamples."),
            HttpServerErrorException.class, ResourceAccessException.class);
    }

    private ResponseEntity<SampleResponse> executeHttpGet(
        RestTemplate restTemplate , HttpHeaders headers, String sampleId, boolean test){

        return RetryUtils.executeWithRetry(
            context -> restTemplate.exchange(
                getWebinRestUri("cli/reference/sample/{id}", test),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SampleResponse.class,
                sampleId.trim()),
            context -> LOGGER.warn("Retrying sample retrieval from server."),
            HttpServerErrorException.class, ResourceAccessException.class);
    }

    private static class SampleResponse {
        public int taxId;
        public String id;
        public String organism;
        public String bioSampleId;
        public boolean canBeReferenced;
    }
}
