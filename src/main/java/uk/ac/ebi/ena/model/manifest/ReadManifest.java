package uk.ac.ebi.ena.model.manifest;

public class ReadManifest extends Manifest<SequenceManifest.FileType> {

    public enum FileType {
        BAM,
        CRAM,
        FASTQ
    }
}
