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

import java.util.ArrayList;
import java.util.List;
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
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class SampleService extends WebinService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SampleService.class);

  public static final String BIOSAMPLES_ID_PREFIX = "SAM";

  private final String biosamplesWebinAuthToken;

  private final BiosamplesService biosamplesService;

  private final SampleXmlService sampleXmlService;

  public static class Builder extends AbstractBuilder<SampleService> {
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
    public SampleService build() {
      return new SampleService(this);
    }
  }

  protected SampleService(Builder builder) {
    super(builder);

    this.biosamplesWebinAuthToken = builder.biosamplesWebinAuthToken;

    biosamplesService =
        new BiosamplesService(
            builder.webinAuthUri,
            builder.biosamplesUri,
            builder.biosamplesWebinUserName,
            builder.biosamplesWebinPassword);

    sampleXmlService =
        new SampleXmlService.Builder()
            .setWebinRestV1Uri(getWebinRestV1Uri())
            .setWebinRestV2Uri(getWebinRestV2Uri())
            .setAuthToken(getAuthToken())
            .setUserName(getUserName())
            .setPassword(getPassword())
            .build();
  }

  /**
   * Depending on what the given sample ID looks like, sample information will be retrieved from
   * either ENA or Biosamples. Also, if the sample is not retrieved from Biosamples then an attempt
   * will be made to retrieve it from ENA.
   */
  public Sample getSample(String sampleId) {
    final List<String> sampleRetrievalMessages = new ArrayList<>();
    boolean isBiosamplesRetrievalAlreadyAttempted = false;
    boolean sraSampleRetrievalFailed = false;
    Sample biosamplesSample;

    // If the ID looks like a BioSamples accession, try BioSamples first
    if (isBiosamplesId(sampleId)) {
      try {
        biosamplesSample = getBiosamplesSample(sampleId);
        isBiosamplesRetrievalAlreadyAttempted = true;
      } catch (final Exception biosampleRetrievalException) {
        biosamplesSample = null;
      }

      // No record returned from BioSamples
      if (biosamplesSample == null) {
        sampleRetrievalMessages.add(ServiceMessage.BIOSAMPLES_NOT_FOUND_MESSAGE.format(sampleId));
      }
      // Valid BioSamples record: return immediately
      else if (isBiosamplesSampleValid(biosamplesSample)) {
        return biosamplesSample;
      }
      // BioSamples record present but not usable
      else {
        sampleRetrievalMessages.add(ServiceMessage.BIOSAMPLES_INVALID_MESSAGE.format(sampleId));
      }
    }

    // Otherwise (or if BioSamples was not usable), fallback to ENA retrieval afterward...
    // If the sample couldn't be retrieved from Biosamples above, then retrieve it from ENA.
    Sample sraSample;

    try {
      sraSample = getSraSample(sampleId);
    } catch (ServiceException e) {
      LOGGER.error("Failed to retrieve SRA sample: {}", sampleId, e);

      sraSample = null;
    }

    if (sraSample == null) {
      sampleRetrievalMessages.add(ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleId));
      sraSampleRetrievalFailed = true;
    }

    // If an SRA sample has a Biosamples accession, then try to retrieve it from Biosamples using
    // this accession. This is because getting samples data from Biosamples is always preferred.
    // This is done only when a previous attempt to retrieve from BioSamples is not performed, so
    // there
    // is no risk of duplicate checks and duplicate messages getting added to the list
    if (sraSample != null
        && sraSample.getBioSampleId() != null
        && !isBiosamplesRetrievalAlreadyAttempted) {
      biosamplesSample = getBiosamplesSample(sraSample.getBioSampleId());

      if (biosamplesSample == null) {
        sampleRetrievalMessages.add(ServiceMessage.BIOSAMPLES_NOT_FOUND_MESSAGE.format(sampleId));
      } else if (isBiosamplesSampleValid(biosamplesSample)) {
        return biosamplesSample;
      } else {
        sampleRetrievalMessages.add(ServiceMessage.BIOSAMPLES_INVALID_MESSAGE.format(sampleId));
      }
    }

    // Getting here means we couldn't get a sample from Biosamples. So return the SRA sample instead
    // after adding attribute information to it. This is performed if SRA sample retrieval is not
    // attempted earlier
    if (!sraSampleRetrievalFailed) {
      Sample sampleFromXml = sampleXmlService.getSample(sraSample.getSraSampleId());

      if (sampleFromXml == null) {
        sampleRetrievalMessages.add(
            ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleId));

        throw new ServiceException(sampleRetrievalMessages);
      }

      sraSample.setAttributes(sampleFromXml.getAttributes());
    } else {
      throw new ServiceException(sampleRetrievalMessages);
    }

    return sraSample;
  }

  public static boolean isBiosamplesId(String biosampleId) {
    return biosampleId.toUpperCase().startsWith(BIOSAMPLES_ID_PREFIX);
  }

  /** The returned sample will not have attribute information. */
  private Sample getSraSample(String sampleId) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<SampleResponse> response =
          executeHttpGet(restTemplate, getAuthHeader(), sampleId);
      SampleResponse sampleResponse = response.getBody();

      if (sampleResponse == null || !sampleResponse.canBeReferenced) {
        return null;
      }

      Sample sample = new Sample();

      sample.setBioSampleId(sampleResponse.bioSampleId);
      sample.setName(sampleResponse.alias);
      sample.setTaxId(sampleResponse.taxId);
      sample.setOrganism(sampleResponse.organism);
      sample.setSraSampleId(sampleResponse.id);

      return sample;
    } catch (final Exception e) {
      throw new ServiceException("Sample retrieval failed from Webin-REST");
    }
  }

  /** Returned sample will contain attribute information as well. */
  private Sample getBiosamplesSample(String sampleId) {
    return biosamplesService.getSample(sampleId, biosamplesWebinAuthToken);
  }

  /**
   * @return false if given Biosample sample is either null or has incomplete information i.e. Tax
   *     ID. True otherwise.
   */
  private boolean isBiosamplesSampleValid(Sample sample) {
    return sample.getAttributes().stream()
        .anyMatch(attribute -> attribute.getName().equalsIgnoreCase("organism"));
  }

  private ResponseEntity<SampleResponse> executeHttpGet(
      RestTemplate restTemplate, HttpHeaders headers, String sampleId) {

    return RetryUtils.executeWithRetry(
        context ->
            restTemplate.exchange(
                resolveAgainstWebinRestV1Uri("cli/reference/sample/{id}"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SampleResponse.class,
                sampleId.trim()),
        context -> LOGGER.warn("Retrying sample retrieval from server."),
        HttpServerErrorException.class,
        ResourceAccessException.class);
  }

  private static class SampleResponse {
    public int taxId;
    public String id;
    public String organism;
    public String bioSampleId;
    public String alias;
    public boolean canBeReferenced;
  }
}
