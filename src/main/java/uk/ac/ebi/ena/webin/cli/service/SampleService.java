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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceException;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceMessage;
import uk.ac.ebi.ena.webin.cli.utils.RetryUtils;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.util.ArrayList;
import java.util.List;

public class 
SampleService extends WebinService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleService.class);

    public static final String SERVICE_NAME = "Sample";

    public static final String BIOSAMPLES_ID_PREFIX = "SAM";

    private final String biosamplesWebinAuthToken;

    private final BiosamplesService biosamplesService;

    private final SampleXmlService sampleXmlService;

    public static class
    Builder extends AbstractBuilder<SampleService> {
        protected String webinAuthUri;
        protected String biosamplesUri;
        protected String biosamplesWebinAuthToken;
        protected String biosamplesWebinUserName;
        protected String biosamplesWebinPassword;

        @Override
        public Builder setWebinRestV1Uri(String webinRestV1Uri) {
            super.setWebinRestV1Uri(webinRestV1Uri);
            return this;
        }

        @Override
        public Builder setWebinRestV2Uri(String webinRestV2Uri) {
            super.setWebinRestV2Uri(webinRestV2Uri);
            return this;
        }

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
        public Builder setAuthToken(String authToken) {
            super.setAuthToken(authToken);
            return this;
        }

        public Builder setWebinAuthUri(String webinAuthUri) {
            this.webinAuthUri = webinAuthUri;
            return this;
        }

        public Builder setBiosamplesUri(String biosamplesUri) {
            this.biosamplesUri = biosamplesUri;
            return this;
        }

        public Builder setBiosamplesWebinAuthToken(String biosamplesWebinAuthToken) {
            this.biosamplesWebinAuthToken = biosamplesWebinAuthToken;
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

    protected 
    SampleService( Builder builder )
    {
        super( builder );

        this.biosamplesWebinAuthToken = builder.biosamplesWebinAuthToken;

        biosamplesService = new BiosamplesService(builder.webinAuthUri, builder.biosamplesUri,
            builder.biosamplesWebinUserName, builder.biosamplesWebinPassword);

        sampleXmlService = new SampleXmlService.Builder()
            .setWebinRestV1Uri(getWebinRestV1Uri())
            .setWebinRestV2Uri(getWebinRestV2Uri())
            .setAuthToken(getAuthToken())
            .setUserName(getUserName())
            .setPassword(getPassword())
            .build();
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

        ResponseEntity<SampleResponse> response = executeHttpGet( restTemplate ,  getAuthHeader(),  sampleId);

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
            sampleId, biosamplesWebinAuthToken);
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

    private ResponseEntity<SampleResponse> executeHttpGet(
        RestTemplate restTemplate , HttpHeaders headers, String sampleId){

        return RetryUtils.executeWithRetry(
            context -> restTemplate.exchange(
                resolveAgainstWebinRestV1Uri("cli/reference/sample/{id}"),
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
