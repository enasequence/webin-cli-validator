package uk.ac.ebi.ena.api;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.ena.model.manifest.Manifest;

public interface SubmissionInterface {
    void validate(Manifest manifest) throws ValidationEngineException;
}
