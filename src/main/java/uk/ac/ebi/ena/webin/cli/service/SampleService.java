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
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.net.URI;
import java.util.Arrays;

public class 
SampleService extends WebinService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleService.class);

    public static final String SERVICE_NAME = "Sample";

    private final static String TEST_AUTH_URL = "https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    private final static String PRODUCTION_AUTH_URL = "https://www.ebi.ac.uk/ena/submit/webin/auth/token";

    public static final String BIOSAMPLES_ID_PREFIX = "SAM";

    public final BiosamplesService biosamplesService = new BiosamplesService();

    protected 
    SampleService( AbstractBuilder<SampleService> builder )
    {
        super( builder );
    }

    public static class
    Builder extends AbstractBuilder<SampleService> {
        @Override
        public SampleService
        build() {
            return new SampleService(this);
        }
    }

    /**
     * Depending on what the given sample ID looks like, sample information will be retrieved from either ENA or Biosamples.
     * Also, if the sample is not found on Biosamples then an attempt will be made to retrieve it from ENA.
     */
    public Sample getSample(String sampleId) {
        Sample sample = null;

        if (sampleId.toUpperCase().startsWith(BIOSAMPLES_ID_PREFIX)) {
            sample = getBiosamplesSample(sampleId);
        }

        // If, either, sample is not found on Biosamples or has incomplete metadata (e.g. Tax ID) then it most likely
        // means Biosamples is not the authority on it. In which case, load the sample from ENA.
        if (sample == null || sample.getTaxId() == null) {
            sample = getSraSample(sampleId);
        }

        return sample;
    }

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

    private Sample getBiosamplesSample(String sampleId) {
        String authToken = getAuthToken();
        if (authToken == null) {
            authToken = generateAuthToken();
        }

        uk.ac.ebi.biosamples.model.Sample biosamplesSample = biosamplesService.findSampleById(sampleId, authToken);
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

        return sample;
    }

    private String generateAuthToken() {
        String authUrl = getTest() ? TEST_AUTH_URL : PRODUCTION_AUTH_URL;

        WebinAuthClientService webinAuthClientService = new WebinAuthClientService(
            new RestTemplateBuilder(),
            URI.create(authUrl),
            getUserName(),
            getPassword(),
            Arrays.asList(AuthRealm.ENA));

        return RetryUtils.executeWithRetry(
            context -> webinAuthClientService.getJwt(),
            context -> LOGGER.warn("Retrying acquiring authentication token."),
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
