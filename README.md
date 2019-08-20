# webin-cli-validator

Contains common classes which acts as an interface between webin-cli and the context specific validators.

## Key concepts

### Context 

The context uniquely identifies the manifest parser, the validator and the submission objects that have to be used. Currently supported contexts are:
  1. reads: raw reads (BAM, CRAM, Fastq) context (validation is part of readtools project)
  2. genome: genome assembly validation context (validation is part of sequencetools project)
  3. transcriptome: transcriptome assembly validation context  (validation is part of sequencetools project)
  4. template: annotated sequence (cvs, flatfile) validation context (validation is part of sequencetools project)

### Manifest 

A manifest provides all the meta and file information required to validate a submission. The manifest fields are specific to each context and the webin-cli-validator contains a specific manifest object for each of the contexts. The manifest objects are created by the Webin-CLI from the submitter provided manifest files.

An extract of a genome context manifest file:
``` 
NAME assembly1
MINGAPLENGTH 100
FLATFILE	test_wgs.embl.gz
AGP	test_chromosome.agp.gz
CHROMOSOME_LIST	test_chromosome.txt.gz
```

### Validator

A validator accepts a context specific manifest object, writes any errors into report files, and returns the validation result as a *ValidationResponse* object.

## Validator implementation

- All validators must implement the *uk.ac.ebi.ena.webin.cli.validator.api.Validator*  interface.
- The *Validator* interface has a single method *validate* that accepts a context specific manifest object and returns a 
*uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse* object.
- Webin-CLI will take care of reading and contructing the manifest object and invoking the validator.
- The *ValidationResponse* contains the validation status that is either is either *VALIDATION_SUCCESS* or *VALIDATION_ERROR*.
- If the *validate*  method throws any exception then this is considered a system error by Webin-CLI.

### Validator implementation example
 ```
 public class YourSubmissionValidator implements Validator {
    @Override
    public ValidationResponse validate(Manifest manifest) {
        if (isYourValidationSuccessful(manifest)) {
            return new ValidationResponse(ValidationResponse.status.VALIDATION_SUCCESS);
        }
        else {
            return new ValidationResponse(ValidationResponse.status.VALIDATION_ERROR);
        }
     }
  }
  ```

More complex implementation example can be found in https://github.com/enasequence/sequencetools/blob/master/src/main/java/uk/ac/ebi/embl/api/validation/submission/SubmissionValidator.java

### Reports
The ValidationResponse object just returns the state, we write all the validation messages and any exception messages or exception stack trace into set of report files. Webin-CLI currently generates three kind of reports.
  1. Report file specific to each submission file : Placed under <submission_files__directory>/\<context\>/\<name\>/validate/\<file_name\>.report. Manifest has list of uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFile objects, each submission file should contain a file type, actual submitted file and a report file to write all the validation message specific to actual submitted file.Webin-CLI will take care of this business. 
  2. webin-cli.report : Available in home directory where all the submission files are exists, e.g., <submission_files_directory>/webin-cli.report. A short summary and/or exception stacktrace of ubnormal termination of webin-CLI execution.
  3. webin-cli.report : Messages which can’t be attributed to a specific input file. Placed under \<submission_files__directory\>/\<context\>/\<name\>/validate/webin-cli.report
## Manifest implementation

- Please provide us with the desired manifest file fields for your specific context. 
- A context specific manifest object will be made available in the webin-cli-validator project by the Webin-CLI maintainers after an agreement on the manifest fields with the validator implementors.
 - The context specific manifest classes extend the *uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest* abstract class. Please check *uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest* for an example.
 - The *Manifest* abstract class has all the common properties available to all contexts. It is extended to add the context spefic properties.

## Adding the validator to Webin-CLI

- Please make your validator available in a public maven repository as a fat JAR containing all required dependencies.
- Please send the artifact details to the Webin-CLI maintainers. 
- The Webin-CLI maintainers will add your JAR dependency into Webin-CLI and implement a manifest reader as well as xml creation and submission functionality for your context.

## Webin-CLI maintainers

- Rasko Leinonen: rasko@ebi.ac.uk
- Senthilnathan Vijayaraja: vijayaraja@ebi.ac.uk
