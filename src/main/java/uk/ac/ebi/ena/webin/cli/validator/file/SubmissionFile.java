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

/** Submitted file.
*/
public class SubmissionFile<FileType extends Enum<FileType>> {

    private FileType fileType;
    private File file;

    private List<Map.Entry<String, String>> attributes;

    /** Validation messages must be written into this file.
     */
    private File reportFile;

    public SubmissionFile(FileType fileType, File file) {
        this.fileType = fileType;
        this.file = file;
        this.attributes = null;
        this.reportFile = null;
    }

    public SubmissionFile(FileType fileType, File file, File reportFile) {
        this.fileType = fileType;
        this.file = file;
        this.attributes = null;
        this.reportFile = reportFile;
    }

    public SubmissionFile(FileType fileType, File file, List<Map.Entry<String, String>> attributes) {
        this.fileType = fileType;
        this.file = file;
        this.attributes = attributes;
        this.reportFile = null;
    }

    public SubmissionFile(FileType fileType, File file, List<Map.Entry<String, String>> attributes, File reportFile) {
        this.fileType = fileType;
        this.file = file;
        this.attributes = attributes;
        this.reportFile = reportFile;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getReportFile() {
        return reportFile;
    }

    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }

    public boolean isFileType(FileType fileType) {
        return fileType != null && fileType.equals(this.fileType);
    }

    public List<Map.Entry<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Map.Entry<String, String>> attributes) {
        this.attributes = attributes;
    }
}
