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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.net.URI;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceException;
import uk.ac.ebi.ena.webin.cli.utils.RetryUtils;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

/**
 * This class originally used Biosamples official client SDK to retrieve samples from Biosamples.
 * The client SDK version that was in use before its removal from this project depended on spring
 * boot/framework. The version of spring it used then had critical security vulnerabilities which
 * could only be resolved by upgrading the major version of spring which Biosamples needed more time
 * to do. Therefore, we were asked by them to stop using the client SDK and use Biosamples REST API
 * directly to retrieve sample data until they are able to sort this problem out and after which we
 * can go back to using it again like before.
 *
 * <p>When this happens and should we then decide to start using the SDK, the quickest way to do
 * that would be to restore the code that used the client before it got removed from git. Just look
 * for this class in version 1.10.0 of this project or git commit
 * 64a32c9237acb88b49530c13eb5626f6436cba9f
 */
class BiosamplesService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BiosamplesService.class);

  private final String webinAuthUri;
  private final String biosamplesUri;
  private final String biosamplesWebinUserName;
  private final String biosamplesWebinPassword;

  public BiosamplesService(
      String webinAuthUri,
      String biosamplesUri,
      String biosamplesWebinUserName,
      String biosamplesWebinPassword) {

    this.webinAuthUri = webinAuthUri;
    this.biosamplesUri = biosamplesUri;
    this.biosamplesWebinUserName = biosamplesWebinUserName;
    this.biosamplesWebinPassword = biosamplesWebinPassword;
  }

  public Sample getSample(String accession, String webinAuthToken) {
    if (webinAuthToken == null || webinAuthToken.isEmpty()) {
      webinAuthToken = getAuthToken();
    }

    return getSampleInternal(webinAuthToken, accession);
  }

  private String getAuthToken() {
    try {
      String bodyStr =
          """
                {
                  "username": "%s",
                  "password": "%s",
                  "authRealms": ["ENA"]
                }
              """;

      bodyStr = String.format(bodyStr, biosamplesWebinUserName, biosamplesWebinPassword);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> entity = new HttpEntity(bodyStr, headers);

      RestTemplate restTemplate = new RestTemplate();

      return RetryUtils.executeWithRetry(
          context ->
              restTemplate
                  .exchange(this.webinAuthUri, HttpMethod.POST, entity, String.class)
                  .getBody(),
          context -> LOGGER.warn("Retrying acquiring authentication token from webin auth."),
          HttpServerErrorException.class,
          ResourceAccessException.class);
    } catch (Exception ex) {
      throw new ServiceException(ex, "Unexpected error getting authentication token.");
    }
  }

  private Sample getSampleInternal(String token, String accession) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + token);

      URI sampleGetUri =
          UriComponentsBuilder.fromUri(URI.create(biosamplesUri + "/v2/samples/" + accession))
              .build(true)
              .toUri();

      RequestEntity<Void> requestEntity = new RequestEntity(headers, HttpMethod.GET, sampleGetUri);

      RestTemplate restTemplate = new RestTemplate();

      JsonNode result =
          RetryUtils.executeWithRetry(
              context -> restTemplate.exchange(requestEntity, JsonNode.class).getBody(),
              context -> LOGGER.warn("Retrying sample retrieval from Biosamples."),
              HttpServerErrorException.class,
              ResourceAccessException.class);

      return transform(result);
    } catch (HttpClientErrorException.NotFound ex) {
      return null;
    } catch (Exception ex) {
      throw new ServiceException(ex, "Unexpected error getting sample.");
    }
  }

  private Sample transform(JsonNode sampleJson) {
    Sample sample = new Sample();
    sample.setBioSampleId(sampleJson.get("accession").asText());
    sample.setName(sampleJson.get("name").asText());

    sample.setAttributes(extractAttributes(sampleJson));

    Attribute organismAttribute =
        sample.getAttributes().stream()
            .filter(attribute -> attribute.getName().equalsIgnoreCase("organism"))
            .findFirst()
            .orElse(null);
    if (organismAttribute != null) {
      sample.setOrganism(organismAttribute.getValue());
    }

    sample.setTaxId(extractTaxId(sampleJson, organismAttribute));

    return sample;
  }

  private List<Attribute> extractAttributes(JsonNode sampleJson) {
    List<Attribute> attributes = new ArrayList<>();

    sampleJson
        .get("characteristics")
        .fields()
        .forEachRemaining(
            field -> {
              String characteristicName = field.getKey();
              ArrayNode characteristicItems = (ArrayNode) field.getValue();

              characteristicItems.forEach(
                  characteristicItem -> {
                    Set<String> iris = new TreeSet<>();
                    if (characteristicItem.hasNonNull("ontologyTerms")) {
                      characteristicItem
                          .get("ontologyTerms")
                          .forEach(iri -> iris.add(iri.textValue()));
                    }

                    Attribute attribute =
                        new Attribute(
                            characteristicName,
                            characteristicItem.get("text").asText(),
                            iris,
                            characteristicItem.has("unit")
                                ? characteristicItem.get("unit").asText()
                                : null,
                            null);

                    attributes.add(attribute);
                  });
            });

    return attributes;
  }

  private Integer extractTaxId(JsonNode sampleJson, Attribute organismAttribute) {
    if (sampleJson.hasNonNull("taxId")) {
      return sampleJson.get("taxId").asInt();
    } else if (organismAttribute != null && !organismAttribute.getIri().isEmpty()) {
      return organismAttribute.getIri().stream()
          .map(this::extractTaxIdFromIri)
          .filter((taxId) -> taxId > 0)
          .findFirst()
          .orElse(null);
    }

    return null;
  }

  private int extractTaxIdFromIri(String iri) {
    if (iri.isEmpty()) {
      return 0;
    } else {
      String[] segments = iri.split("NCBITaxon_");

      try {
        return Integer.parseInt(segments[segments.length - 1]);
      } catch (NumberFormatException ex) {
        return 0;
      }
    }
  }
}
