/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
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
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class BiosamplesServiceTest {

  @Test
  public void testPublicSample() {
    BiosamplesService biosamplesService =
        new BiosamplesService(
            SampleServiceTest.WEBIN_AUTH_URI,
            SampleServiceTest.BIOSAMPLES_URI,
            System.getenv("biosamples-webin-username"),
            System.getenv("biosamples-webin-password"));

    Sample sample = biosamplesService.getSample("SAMEA13774371", null);

    Assert.assertNotNull(sample);
    Assert.assertEquals("SSC_UEDIN_GS_WP2_21_ISO_SAMPLES_POOL", sample.getName());
    Assert.assertEquals("SAMEA13774371", sample.getBioSampleId());
    Assert.assertEquals("Sus scrofa", sample.getOrganism());
    Assert.assertEquals(9823, sample.getTaxId().longValue());
    Assert.assertEquals(318, sample.getAttributes().size());
  }

  @Test
  public void testInvalidSample() {
    BiosamplesService biosamplesService =
        new BiosamplesService(
            SampleServiceTest.WEBIN_AUTH_URI,
            SampleServiceTest.BIOSAMPLES_URI,
            System.getenv("biosamples-webin-username"),
            System.getenv("biosamples-webin-password"));

    Sample sample = biosamplesService.getSample("xxx", null);

    Assert.assertNull(sample);
  }
}
