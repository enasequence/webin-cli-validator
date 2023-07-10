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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class
SampleServiceTest {

    public static final String WEBIN_REST_URI = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/";
    public static final String WEBIN_AUTH_URI = "https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    public static final String BIOSAMPLES_URI = "https://wwwdev.ebi.ac.uk/biosamples/";

    private static final String WEBIN_ACCOUNT_USERNAME = System.getenv("webin-username");
    private static final String WEBIN_ACCOUNT_PASSWORD = System.getenv("webin-password");

    private static final String BIOSAMPLES_WEBIN_ACCOUNT_USERNAME = System.getenv("biosamples-webin-username");
    private static final String BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD = System.getenv("biosamples-webin-password");

    private static final String BIO_SAMPLE_ID = "SAMEA749881";
    private static final String SAMPLE_ID = "ERS000002";
    private static final String SCIENTIFIC_NAME = "Saccharomyces cerevisiae SK1";
    private static final int TAX_ID = 580239;
    private static final String SAMPLE_NAME = "Solexa sequencing of Saccharomyces cerevisiae strain SK1 random 200 bp library";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetSampleUsingPublicBioSampleId() {
        testGetSampleUsingValidId(BIO_SAMPLE_ID);
    }

    @Test
    public void testGetSampleUsingPublicSampleId() {
        testGetSampleUsingValidId(SAMPLE_ID);
    }

    @Test
    public void testGetSampleUsingInvalidId() {
        String id = "INVALID";
        exceptionRule.expect(HttpClientErrorException.NotFound.class);
        SampleService sampleService = new SampleService.Builder()
            .setWebinRestV1Uri(WEBIN_REST_URI)
            .setUserName( WEBIN_ACCOUNT_USERNAME )
            .setPassword( WEBIN_ACCOUNT_PASSWORD )
            .setWebinAuthUri(WEBIN_AUTH_URI)
            .setBiosamplesUri(BIOSAMPLES_URI)
            .setBiosamplesWebinUserName(BIOSAMPLES_WEBIN_ACCOUNT_USERNAME)
            .setBiosamplesWebinPassword(BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD)
            .build();
        sampleService.getSample( id );
    }

    @Test
    public void testSampleRetrievalFallback() {
        // This ID represents a sample which is private and does not contain full information on Biosamples. It offers
        // a nice opportunity to test ENA fallback i.e. if sample cannot be retrieved from Biosamples then it
        // will be retrieved from ENA instead.
        String id = "SAMEA9403245";

        SampleService sampleService = new SampleService.Builder()
            .setWebinRestV1Uri(WEBIN_REST_URI)
            .setUserName( WEBIN_ACCOUNT_USERNAME )
            .setPassword( WEBIN_ACCOUNT_PASSWORD )
            .setWebinAuthUri(WEBIN_AUTH_URI)
            .setBiosamplesUri(BIOSAMPLES_URI)
            .setBiosamplesWebinUserName(BIOSAMPLES_WEBIN_ACCOUNT_USERNAME)
            .setBiosamplesWebinPassword(BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD)
            .build();

        Sample sample = sampleService.getSample( id );

        assertThat(sample).isNotNull();
        assertThat(sample.getTaxId()).isEqualTo(9606);
        assertThat(sample.getOrganism()).isEqualTo("Homo sapiens");
        assertThat(sample.getName()).isEqualTo("test_custom");
    }

    @Test
    public void testSampleIdIsBiosamplesId() {
        assertThat(SampleService.isBiosamplesId(BIO_SAMPLE_ID)).isTrue();
        assertThat(SampleService.isBiosamplesId(SAMPLE_ID)).isFalse();
    }

    private void testGetSampleUsingValidId(String id) {
        SampleService sampleService = new SampleService.Builder()
            .setWebinRestV1Uri(WEBIN_REST_URI)
            .setUserName( WEBIN_ACCOUNT_USERNAME )
            .setPassword( WEBIN_ACCOUNT_PASSWORD )
            .setWebinAuthUri(WEBIN_AUTH_URI)
            .setBiosamplesUri(BIOSAMPLES_URI)
            .setBiosamplesWebinUserName(BIOSAMPLES_WEBIN_ACCOUNT_USERNAME)
            .setBiosamplesWebinPassword(BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD)
            .build();
        Sample sample = sampleService.getSample( id );
        assertThat(sample).isNotNull();
        assertThat(sample.getBioSampleId()).isEqualTo(BIO_SAMPLE_ID);
        assertThat(sample.getOrganism()).isEqualTo(SCIENTIFIC_NAME);
        assertThat(sample.getTaxId()).isEqualTo(TAX_ID);
        assertThat(sample.getName()).isEqualTo(SAMPLE_NAME);
    }
}
