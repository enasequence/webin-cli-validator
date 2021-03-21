/*
 * Copyright 2018-2021 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.ena.webin.cli.validator.file;

import java.io.File;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.ena.webin.cli.validator.message.ValidationReport;

/**
 * Submitted file.
 */
public class SubmissionFile<FileType extends Enum<FileType>> {

    private final FileType fileType;
    private final File file;
    private final ValidationReport validationReport;
    private final List<Map.Entry<String, String>> attributes;

    public SubmissionFile(FileType fileType, File file, ValidationReport validationReport) {
        this.fileType = fileType;
        this.file = file;
        this.validationReport = validationReport;
        this.attributes = null;
    }

    public SubmissionFile(FileType fileType, File file, ValidationReport validationReport, List<Map.Entry<String, String>> attributes) {
        this.fileType = fileType;
        this.file = file;
        this.validationReport = validationReport;
        this.attributes = attributes;
    }

    public FileType getFileType() {
        return fileType;
    }

    public File getFile() {
        return file;
    }

    public boolean isFileType(FileType fileType) {
        return fileType != null && fileType.equals(this.fileType);
    }

    public List<Map.Entry<String, String>> getAttributes() {
        return attributes;
    }
}
