# webin-cli-validator

Contains common classes which acts as an interface between webin-cli and validator.

**Keywords**

- context : A mandatory parameter for webin-cli tool. A functional unit, uniquelly identifies which parser,validator and submission objects has to be used. Example contexts are,
       reads - supports all raw reads validation and submission(validation is part of readtools project).
       genome - supports all genome assembly validation and submission. (validation is part of sequencetools project).
       transcriptome - trnscriptome submisison validation and submission. (validation is part of sequencetools project).
       template - csv/flatfile template submissions. (validation is part of sequencetools project).
- manifest : Another mandatory field , a apace separated key-value pair file , which provides all the mandatory meta information required for the validator to validate specific context. Properties are specific to each context, so we need spefic manifest reader spefic to context.

Sample genome context manifest:
``` 
FLATFILE	test_wgs.embl.gz
AGP	test_chromosome.agp.gz
CHROMOSOME_LIST	test_chromosome.txt.gz
MINGAPLENGTH 100
```
**Implementation**

- All the validators pluggable in webin-cli should implement interface uk.ac.ebi.ena.webin.cli.validator.api.Validator.
- Validator has a single method which accepts any manifest (class with all the required properties for the validator) which extends uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest<FileType>. Please check uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest for an example.
- Implementing class should contruct and return the uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse with status and all the messages.
- webin-cli will take care of reading and contructing Manifest object and invoking validator.
**Extending Manifest:**

   Manifest extension for a specific context should be available in webin-cli-validator.
   
   uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest has all the common properties required for all contexts Webin-cli currently supporting. Extend Manifest class to add all the context spefic properties.  Please check GenomeManifest.java for sample extension.
   
Please finalize and provide us the manifest file structure, we will implememnt the manifest parser for your context. Manifest parser will you give you the Manifest specific to your context, which can be passed to teh validate method in Validator implementation.  


**Sample implementation of Validator interface:**
 ```
 public class SubmissionValidator implements Validator {
    @Override
    public ValidationResponse validate(Manifest manifest) {
    //TODO: your implementation goes here
        ValidationResponse response = new ValidationResponse(ValidationResponse.status.VALIDATION_SUCCESS);
        try {
            //All the pre-processing of Manifest objectValidate manifest objects, convert to your validator specific object(if required) 
            SubmissionOptions options = mapManifestToSubmissionOptions(manifest);
            
            //Validate method will do all the validation. 
            //Validate should write all teh validation messages in report file mentioned in SubmissionFile object.
            //Also, add any messages in response as given in catch block, if required.
            validate(options, response);
        } catch (ValidationEngineException vee) {
            switch (vee.getErrorType()) {
                case VALIDATION_ERROR:
                    response.setStatus(ValidationResponse.status.VALIDATION_ERROR);
                    response.addMessage(vee.getMessage());
                    break;
                default:
                    throw new RuntimeException(vee);
            }
        }
        return response;
    }
  }
  ```

More complex implementation can be found in https://github.com/enasequence/sequencetools/blob/master/src/main/java/uk/ac/ebi/embl/api/validation/submission/SubmissionValidator.java

**Adding dependency in webin-cli**

-Build a jar for the implementing validator and add it to the library path or publish it to maven or any accessible repository and  add it as dependency in webin-cli.

e.g., 
```
implementation( group: 'uk.ac.ebi.ena.sequence', name: 'sequencetools', version: '2.0.34' )
```

Please contact <Rasko> rasko@ebi.ac.uk or <Senthil> vijayaraja@ebi.ac.uk for any clarification.


