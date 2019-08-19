# webin-cli-validator

## Incomplete. Work in progress readme.
Contains common classes which acts as an interface between webin-cli and validator.


- All the validators pluggable in webin-cli should implement interface uk.ac.ebi.ena.webin.cli.validator.api.Validator.
- Validator has a single method which accepts any manifest (class with all the required properties for the validator) which extends uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest<FileType>. Please check uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest for an example.
- Implementing class should contruct and return the uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse with status and all the messages.
- webin-cli will take care of reading and contructing Manifest object and invoking validator.

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

**Extending Manifest:**

   uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest has all the common properties required for all contexts Webin-cli currently supporting. Extend Manifest class to add all the context spefic properties.  Please check GenomeManifest.java for sample extension.
   
Please finalize  the manifest file structure and Rasko at rasko@ebi.ac.uk or Senthil at vijayaraja@ebi.ac.uk for any clarification.

