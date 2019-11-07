package uk.ac.ebi.ena.webin.cli.validator.manifest;

import java.util.*;

public class TaxRefSetManifest extends Manifest<TaxRefSetManifest.FileType> {
    public enum FileType {
        FASTA,
        TAB
    }

    private String taxonomySystem;
    private String taxonomySystemVersion;
    private Map<String,String> customFields = new LinkedHashMap<>();

    public String getTaxonomySystem() {
        return taxonomySystem;
    }

    public void setTaxonomySystem(String taxonomySystem) {
        this.taxonomySystem = taxonomySystem;
    }

    public String getTaxonomySystemVersion() {
        return taxonomySystemVersion;
    }

    public void setTaxonomySystemVersion(String taxonomySystemVersion) {
        this.taxonomySystemVersion = taxonomySystemVersion;
    }

    public Map<String,String> getCustomFields() {
        return customFields;
    }

    public void addCustomField(String key, String value) {
        this.customFields.put(key, value);
    }

    public void addCustomFields(Map<String,String> customFields) {
        this.customFields.putAll(customFields);
    }
}
