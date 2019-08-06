package uk.ac.ebi.ena.model.manifest;

public class SequenceManifest extends Manifest<SequenceManifest.FileType> {

    public enum FileType {
        FLATFILE,
        TAB
    }
}
