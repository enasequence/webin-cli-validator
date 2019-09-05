package uk.ac.ebi.ena.webin.cli.validator.response;

import uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse;

public class ReadsValidationResponse extends ValidationResponse {

    private boolean paired = false;

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

}
