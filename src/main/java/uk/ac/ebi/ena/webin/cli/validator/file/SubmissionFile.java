/*
 * Copyright 2018-2019 EMBL - European Bioinformatics Institute
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

/** Submitted file.
*/
public class SubmissionFile<FileType extends Enum<FileType>> {

    private final FileType fileType;
    private final File file;

    /** Validation messages must be written into this file.
     */
    private final File reportFile;

    public SubmissionFile(FileType fileType, File file) { // TODO: , File reportFile) {
        this.fileType = fileType;
        this.file = file;
        this.reportFile = null; // TODO: reportFile;
    }

    public FileType getFileType() {
        return fileType;
    }

    public boolean isFileType(FileType fileType) {
        return fileType != null && this.fileType != null && fileType.equals(this.fileType);
    }

    public File getFile() {
        return file;
    }

    public File getReportFile() {
        return reportFile;
    }
}
