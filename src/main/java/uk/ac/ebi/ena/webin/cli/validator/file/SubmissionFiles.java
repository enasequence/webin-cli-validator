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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** List of submitted files.
 */
public class SubmissionFiles <FileType extends Enum<FileType>> {

    private List<SubmissionFile<FileType>> files = new ArrayList<>();

    public void set(List<SubmissionFile<FileType>> files) {
        this.files = files;
    }

    public SubmissionFiles<FileType> add(SubmissionFile<FileType> file) {
        this.files.add(file);
        return this;
    }

    public List<SubmissionFile<FileType>> get() {
        return files;
    }

    public List<SubmissionFile<FileType>> get(FileType fileType) {
        return files.stream().filter(file -> file.isFileType(fileType)).collect(Collectors.toList());
    }

    public List<File> files() {
        return files.stream().map(file -> file.getFile()).collect(Collectors.toList());
    }
}
