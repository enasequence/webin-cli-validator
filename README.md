# webin-cli-validator

Contains common classes which acts as an interface between webin-cli and validator.


- All the validators pluggable in webin-cli should implement interface uk.ac.ebi.ena.webin.cli.validator.api.Validator.
- Validaor has a single method which accepts any manifest (class with all the required properties for the validator) which extends uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest<FileType>. Please check uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest for an example.
- Implementing class should contruct and return the uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse with status and all the messages.
- webin-cli will take care of reading and contructing Manifest object and invoking validator.
