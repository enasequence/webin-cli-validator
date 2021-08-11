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
package uk.ac.ebi.ena.webin.cli.validator.manifest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFile;
import uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFiles;
import uk.ac.ebi.ena.webin.cli.validator.reference.Analysis;
import uk.ac.ebi.ena.webin.cli.validator.reference.Run;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;
import uk.ac.ebi.ena.webin.cli.validator.reference.Study;

/**
 * Class with all common parameters, must be extended to create any context specific Manifest
 * @param <FileType>
 */
public abstract class Manifest <FileType extends Enum<FileType>> {

    private String name;
    private String description;
    private String address;
    private String authors;
    private Sample sample;
    private Study study;
    private List<Run> run = new ArrayList<>();
    private List<Analysis> analysis = new ArrayList<>();
    private SubmissionFiles<FileType> files = new SubmissionFiles<>();
    private String submissionTool;
    private String submissionToolVersion;
    private boolean quick;
    private boolean ignoreErrors;
    private String authToken;
    private boolean isTestMode;

    /** Temporary files must written into this directory.
     */
    private File processDir;

    /** Validation messages must be written into this file.
     */
    private File reportFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public List<Run> getRun() {
        return run;
    }

    public void setRun(List<Run> run) {
        this.run = run;
    }

    public void addRun(Run... runs) {
        for (Run run : runs) {
            getRun().add(run);
        }
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<Analysis> analysis) {
        this.analysis = analysis;
    }

    public void addAnalysis(Analysis... analyses) {
        for (Analysis analysis : analyses) {
            getAnalysis().add(analysis);
        }
    }

    public SubmissionFiles<FileType> getFiles() {
        return files;
    }

    public void setFiles(SubmissionFiles<FileType> files) {
        this.files = files;
    }

    public SubmissionFiles<FileType> files() {
        return files;
    }

    public List<SubmissionFile<FileType>> files(FileType fileType) {
        return files.get().stream().filter(file -> file.isFileType(fileType)).collect(Collectors.toList());
    }

    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }

    public File getProcessDir() {
        return processDir;
    }

    public void setProcessDir(File processDir) {
        this.processDir = processDir;
    }

    public File getReportFile() {
        return reportFile;
    }

    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }

    public String getSubmissionTool() {
        return submissionTool;
    }

    public void setSubmissionTool(String submissionTool) {
        this.submissionTool = submissionTool;
    }

    public String getSubmissionToolVersion() {
        return submissionToolVersion;
    }

    public void setSubmissionToolVersion(String submissionToolVersion) {
        this.submissionToolVersion = submissionToolVersion;
    }

    public boolean isQuick() {
        return quick;
    }

    public void setQuick(boolean quick) {
        this.quick = quick;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean getTestMode() {
        return isTestMode;
    }

    public void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }
}
