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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import uk.ac.ebi.biosamples.client.model.auth.AuthRealm;
import uk.ac.ebi.biosamples.client.service.WebinAuthClientService;
import uk.ac.ebi.biosamples.model.Sample;

import java.net.URI;
import java.util.Arrays;

public class BiosamplesServiceTest {

    final WebinAuthClientService webinAuthClientService = new WebinAuthClientService(
        new RestTemplateBuilder(),
        URI.create("https://www.ebi.ac.uk/ena/submit/webin/auth/token"),
        "Webin-256",
        "sausages",
        Arrays.asList(AuthRealm.ENA)
    );

    @Test
    public void testPublicSample() {
        BiosamplesService biosamplesService = new BiosamplesService();

        Sample sample = biosamplesService.findSampleById("SAMEA13774371", webinAuthClientService.getJwt());

        Assert.assertNotNull(sample);
        Assert.assertEquals("SSC_UEDIN_GS_WP2_21_ISO_SAMPLES_POOL", sample.getName());
        Assert.assertEquals("SAMEA13774371", sample.getAccession());
        Assert.assertEquals(9823, sample.getTaxId().longValue());
        Assert.assertEquals("Sus scrofa", sample.getAttributes().stream()
            .filter(attr -> attr.getType().equals("organism"))
            .findFirst().map(attr -> attr.getValue())
            .get());
    }
}
