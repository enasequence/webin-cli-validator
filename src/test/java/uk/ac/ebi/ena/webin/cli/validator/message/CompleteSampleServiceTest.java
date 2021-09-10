package uk.ac.ebi.ena.webin.cli.validator.message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ena.webin.cli.service.CompleteSampleService;
import uk.ac.ebi.ena.webin.cli.service.WebinService;
import uk.ac.ebi.ena.webin.cli.service.exception.ServiceiException;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import static org.junit.Assert.assertEquals;

public class CompleteSampleServiceTest {

    private static final String SAMPLE_ID="ERS7118926";
    private static final String BIO_SAMPLE_ID="SAMEA9403245";
    private static final String SAMPLE_ALIAS="test_custom";
    private final static String AUTH_JSON="{\"authRealms\":[\"ENA\"],\"password\":\"sausages\",\"username\":\"Webin-256\"}";
    private final static String TEST_AUTH_URL="https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    private static String TOKEN="";
    private static CompleteSampleService completeSampleService;
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();


    @Before
    public void init(){
        WebinService.AbstractBuilder<CompleteSampleService> builder=new WebinService.AbstractBuilder<CompleteSampleService>() {
            @Override
            public CompleteSampleService build() {
              this.authToken=getAuthTokenTest();
              this.setTest(true);
              return new CompleteSampleService(this);
            }
        };
        completeSampleService = builder.build();
        
    }

    @Test
    public void testGetSampleFromXmlAPI() {

        // Get sample using valid sample_id
        assertValidSample(SAMPLE_ID);

        // Get sample using valid bio_sample_id
        assertValidSample(BIO_SAMPLE_ID);

        // Get sample using valid sample_alias
        assertValidSample(SAMPLE_ALIAS);

        // Get sample using junk value
        assertInvalidSample("JUNK");
        
    }
    
    private void assertValidSample(String sampleStr){
        Sample sampleObj = completeSampleService.getCompleteSample(sampleStr);
        assertEquals(sampleObj.getBioSampleId(),BIO_SAMPLE_ID);
        assertEquals(sampleObj.getTaxId(),Integer.valueOf("9606"));
        assertEquals(sampleObj.getOrganism(),"Homo sapiens");
        assertEquals(sampleObj.getId(),SAMPLE_ID);
        assertEquals(sampleObj.getAttributes().size(),2);
    }

    private void assertInvalidSample(String sampleStr){
        exceptionRule.expect(ServiceiException.class);
        exceptionRule.expectMessage("Unknown sample JUNK or the sample cannot be referenced by your submission account. Samples must be submitted before they can be referenced in the submission.");
        completeSampleService.getCompleteSample(sampleStr);
    }
  

    private String getAuthTokenTest(){

        if(!StringUtils.isEmpty(TOKEN)){
            return TOKEN;
        }
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<String>(AUTH_JSON, headers);
        ResponseEntity<String> response =
                restTemplate.postForEntity(TEST_AUTH_URL,request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TOKEN = response.getBody();
        return TOKEN;
    }
}
