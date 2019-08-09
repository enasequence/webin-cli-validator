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
package uk.ac.ebi.ena.webin.cli.validator.manifest;

public class TranscriptomeManifest extends Manifest<TranscriptomeManifest.FileType> {

    public enum FileType {
        FASTA,
        FLATFILE,
    }

    private String program;
    private String platform;
    private Boolean tpa;

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Boolean isTpa() {
        if (tpa == null) {
            return false;
        }
        return tpa;
    }

    public void setTpa(Boolean tpa) {
        this.tpa = tpa;
    }
}
