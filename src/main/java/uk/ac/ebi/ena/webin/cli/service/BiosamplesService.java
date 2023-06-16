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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import uk.ac.ebi.biosamples.BioSamplesProperties;
import uk.ac.ebi.biosamples.client.BioSamplesClient;
import uk.ac.ebi.biosamples.client.model.auth.AuthRealm;
import uk.ac.ebi.biosamples.client.service.WebinAuthClientService;
import uk.ac.ebi.biosamples.model.Sample;
import uk.ac.ebi.biosamples.service.AttributeValidator;
import uk.ac.ebi.biosamples.service.SampleValidator;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceException;
import uk.ac.ebi.ena.webin.cli.utils.RetryUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.NoSuchElementException;

class BiosamplesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BiosamplesService.class);

    private final BioSamplesClient bioSamplesClient;

    public BiosamplesService(String webinAuthUri, String biosamplesUri,
                             String biosamplesWebinUserName, String biosamplesWebinPassword) {

        WebinAuthClientService webinAuthClientService = null;
        if (webinAuthUri != null && biosamplesWebinUserName != null && biosamplesWebinPassword != null) {
            webinAuthClientService = new WebinAuthClientService(
                new RestTemplateBuilder(),
                URI.create(webinAuthUri),
                biosamplesWebinUserName,
                biosamplesWebinPassword,
                Arrays.asList(AuthRealm.ENA));
        }

        bioSamplesClient = new BioSamplesClient(
            URI.create(biosamplesUri),
            null,
            new RestTemplateBuilder(),
            new SampleValidator(new AttributeValidator()),
            webinAuthClientService,
            getBioSamplesProperties());
    }

    /**
     * @param accession
     * @param webinAuthToken Can be null. If it is not null then this token will be used for authentication instead.
     * @return
     */
    public Sample findSampleById(String accession, String webinAuthToken) {
        try {
            return RetryUtils.executeWithRetry(
                context -> {
                    if (webinAuthToken == null) {
                        return bioSamplesClient.fetchSampleResourceV2(accession);
                    } else {
                        return bioSamplesClient.fetchSampleResourceV2(accession, webinAuthToken);
                    }
                },
                context -> LOGGER.warn("Retrying sample retrieval from Biosamples."),
                HttpServerErrorException.class, ResourceAccessException.class);

        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            throw new ServiceException(e, "Could not retrieve BioSample [" + accession + "]");
        }
    }

    private BioSamplesProperties getBioSamplesProperties() {
        return new BioSamplesProperties() {
            @Override
            public int getBiosamplesClientPagesize() {
                return 1000;
            }

            @Override
            public int getBiosamplesClientTimeout() {
                return 60000;
            }

            @Override
            public int getBiosamplesClientConnectionCountMax() {
                return 8;
            }

            @Override
            public int getBiosamplesClientThreadCount() {
                return 1;
            }

            @Override
            public int getBiosamplesClientThreadCountMax() {
                return 8;
            }

            @Override
            public int getBiosamplesClientConnectionCountDefault() {
                return 8;
            }

            @Override
            public boolean getAgentSolrStayalive() {
                return false;
            }
        };
    }
}
