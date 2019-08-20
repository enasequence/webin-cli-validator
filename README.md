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

A manifest provides all the meta and file information required to validate a submission. The manifest fields are specific to each context and the webin-cli-validator contains a specific manifest object for each of the contexts. A *Manifest* object is created by the Webin-CLI from the submitter provided manifest files.

An extract of a genome context manifest file:
``` 
NAME assembly1
MINGAPLENGTH 100
FLATFILE	test_wgs.embl.gz
AGP	test_chromosome.agp.gz
CHROMOSOME_LIST	test_chromosome.txt.gz
```

## Manifest implementation

Please provide us with the desired manifest file fields for your specific context. A context specific *Manifest* class will be created Webin-CLI maintainers after an agreement on the manifest fields with the validator implementors.

The context specific *Manifest* class will extend the *uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest* abstract class. Manifest fields defined in the abstract class that are available to all contexts include:
- description
- authors
- address
- sample
- study
- run
- analysis
- files
- ignoreErrors

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

## Validator report files

The *ValidationResponse* only contains the *VALIDATION_SUCCESS* or *VALIDATION_ERROR* state. The validators are expected to write all messages intended for submitters, including error messages and exception stack traces, into report files. The report file locations and names are set by the Webin-CLI and provided to the validators using the *Manifest* object.

### Input file specific report files

Whenever possible, messages should be written into input file specific report files. These report files are defined in the   *uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFile.reportFile* field. They point to '\<output directory\>/\<context\>/\<name\>/validate/\<file\>.report' files where \<file\> corresponds to the input file name that is being validated. The input file is defined in the *uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFile.file* field.

### Submission specific report files

Messages which can’t be attributed to specific input files should be written into the submission specific report file. This report file is defined in the *uk.ac.ebi.ena.webin.cli.validator.file.Manifest.reportFile* field and point to the '\<output directory\>/\<context\>/\<name\>/validate/webin-cli.report' file.

## Validator temporary files

Any temporary files should be written into the *uk.ac.ebi.ena.webin.cli.validator.file.Manifest.processDir* directory.

## Adding the validator to Webin-CLI

- Make your validator available in a public maven repository as a fat JAR containing all required dependencies.
- Send the artifact details to the Webin-CLI maintainers. 
- Webin-CLI maintainers will add your JAR dependency into Webin-CLI and implement a manifest reader as well as xml creation and submission functionality for your context.

## Webin-CLI maintainers

- Rasko Leinonen: rasko@ebi.ac.uk
- Senthilnathan Vijayaraja: vijayaraja@ebi.ac.uk
