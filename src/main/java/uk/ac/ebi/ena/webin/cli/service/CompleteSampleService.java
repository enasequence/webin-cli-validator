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

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import uk.ac.ebi.ena.webin.cli.service.exception.ServiceMessage;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceiException;
import uk.ac.ebi.ena.webin.cli.service.handler.NotFoundErrorHandler;
import uk.ac.ebi.ena.webin.cli.service.utils.HttpHeaderBuilder;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

 /**
    This class is used to get the complete sample i.e sampleXml + sampleResponceObject
    For this it will call samples/{id} and cli/reference/sample/{id} APIs
 */
public class CompleteSampleService extends WebinService
{
    private static final Pattern bioSampleIdPattern = Pattern.compile("^SAM[E,N,D][A-Z]?\\d{6,}$");
    
    public static class 
    Builder extends AbstractBuilder<CompleteSampleService>
    {
        @Override public CompleteSampleService
        build()
        {
            return new CompleteSampleService( this );
        }
    };

    public CompleteSampleService(AbstractBuilder<CompleteSampleService> builder )
    {
        super( builder );
    }

    public Sample getCompleteSample(String sampleValue ) {
        Sample sample= getSampleFromAPI(sampleValue, getTest());
        return sample;
    }

    /**
     * This method will call cli/reference/sample/{id} and get SampleResponse to build Sample object
     * @param sampleValue
     * @param test
     * @return Sample object
     */
    private Sample getSampleFromAPI(String sampleValue, boolean test) {
        
        // The "cli/reference/sample/{id}" API accepts sampleId (or) sampleAlias (or) biosampleId and responds SampleResponse. 
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new NotFoundErrorHandler(
                ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleValue),
                ServiceMessage.SAMPLE_SERVICE_SYSTEM_ERROR.format(sampleValue)));

        HttpHeaders headers = getAuthTokenHeader();
        ResponseEntity response = executeHttpGetSampleRsp(restTemplate, "cli/reference/sample/{id}", headers, sampleValue, test);

        SampleResponse sampleResponse = (SampleResponse) response.getBody();
        if (sampleResponse == null || !sampleResponse.canBeReferenced) {
            throw ServiceiException.userError(ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleValue));
        }
        Sample sample = new Sample();
        sample.setBioSampleId(sampleResponse.bioSampleId);
        sample.setTaxId(sampleResponse.taxId);
        sample.setOrganism(sampleResponse.organism);
        sample.setId(sampleResponse.id);
        setXmlAttribute(sample,test);
        return sample;
    }
    
    private void setXmlAttribute(Sample sample,boolean test){
        sample.setAttributes(getSampleFromXmlAPI( sample.getId(),  test).getAttributes());
    }

    /**
     * This method will call samples/{id} API to get the sample xml and set sampleAttributes to the passed sample
     * @param sampleId
     * @param test
     * @return Sample object
     */
    private Sample getSampleFromXmlAPI(String sampleId, boolean test) {

        RestTemplate restTemplate = getRestTemplate(sampleId);
        HttpHeaders headers = getAuthTokenHeader();
        ResponseEntity<String> response = executeHttpGet(restTemplate, "samples/{id}", headers, sampleId, getTest());
        String sampleXml=response.getBody();
        Sample sample = new Sample();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(sampleXml)));
            doc.getDocumentElement().normalize();

            // name
            NodeList sampleList = doc.getElementsByTagName("SAMPLE");
            for (int i = 0; i < Math.min(1, sampleList.getLength()); i++) {
                Node node = sampleList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    sample.setName(element.getAttribute("alias"));
                }
            }

            // taxid, organism
            NodeList sampleNameList = doc.getElementsByTagName("SAMPLE_NAME");
            for (int i = 0; i < Math.min(1, sampleNameList.getLength()); i++) {
                Node node = sampleNameList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getElementsByTagName("TAXON_ID").getLength() > 0) {
                        sample.setTaxId(Integer.valueOf(element.getElementsByTagName("TAXON_ID").item(0).getTextContent()));
                    }
                    if (element.getElementsByTagName("SCIENTIFIC_NAME").getLength() > 0) {
                        sample.setOrganism(element.getElementsByTagName("SCIENTIFIC_NAME").item(0).getTextContent());
                    }
                }
            }

            // attributes
            NodeList attributeList = doc.getElementsByTagName("SAMPLE_ATTRIBUTE");
            for (int i = 0; i < attributeList.getLength(); i++) {
                Node node = attributeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String tag = element.getElementsByTagName("TAG").item(0).getTextContent();
                    String value = element.getElementsByTagName("VALUE").getLength() > 0 ? element.getElementsByTagName("VALUE").item(0).getTextContent() : null;
                    String units = element.getElementsByTagName("UNITS").getLength() > 0 ? element.getElementsByTagName("UNITS").item(0).getTextContent() : null;
                    sample.addAttribute(new Attribute(tag, value, units));
                }
            }

            return sample;
        } catch (Exception ex) {
            throw ServiceiException.userError(
                    ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleId));
        }
    }

    private RestTemplate getRestTemplate(String sampleId){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new NotFoundErrorHandler(
                ServiceMessage.SAMPLE_SERVICE_VALIDATION_ERROR.format(sampleId),
                ServiceMessage.SAMPLE_SERVICE_SYSTEM_ERROR.format(sampleId)));
        return restTemplate;
    }

    private ResponseEntity<String> executeHttpGet(RestTemplate restTemplate,String url, HttpHeaders headers, String sampleId, boolean test){
        return restTemplate.exchange(
                getWebinRestUri(url, test),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                sampleId.trim());
    }

    private ResponseEntity<SampleResponse> executeHttpGetSampleRsp(RestTemplate restTemplate, String url, HttpHeaders headers, String sampleId, boolean test){
        return restTemplate.exchange(
                getWebinRestUri(url, test),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SampleResponse.class,
                sampleId.trim());
    }
    
    private HttpHeaders getAuthTokenHeader(){
        HttpHeaders headers = new HttpHeaderBuilder().build();
        String bearerToken = "Bearer " + getAuthToken();
        headers.set("Authorization",bearerToken);
        return headers;
    }

    private static class SampleResponse {
        public int taxId;
        public String id;
        public String organism;
        public String bioSampleId;
        public boolean canBeReferenced;
    }

    public static boolean isValidBioSampleId(String bioSampleId){
        Matcher matcher = bioSampleIdPattern.matcher(bioSampleId);
        return matcher.matches();
    }
}
