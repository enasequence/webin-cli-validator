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

public class ReadsManifest extends Manifest<ReadsManifest.FileType> {

  public enum FileType {
    BAM,
    CRAM,
    FASTQ
  }

  public enum QualityScore {
    PHRED_33,
    PHRED_64,
    LOGODDS
  }

  private String platform;
  private String instrument;
  private Integer insertSize;
  private String libraryConstructionProtocol;
  private String libraryName;
  private String librarySource;
  private String librarySelection;
  private String libraryStrategy;
  private QualityScore qualityScore;
  private Integer pairingHorizon = 500_000_000;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Integer getInsertSize() {
        return insertSize;
    }

    public void setInsertSize(Integer insertSize) {
        this.insertSize = insertSize;
    }

    public String getLibraryConstructionProtocol() {
        return libraryConstructionProtocol;
    }

    public void setLibraryConstructionProtocol(String libraryConstructionProtocol) {
        this.libraryConstructionProtocol = libraryConstructionProtocol;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getLibrarySource() {
        return librarySource;
    }

    public void setLibrarySource(String librarySource) {
        this.librarySource = librarySource;
    }

    public String getLibrarySelection() {
        return librarySelection;
    }

    public void setLibrarySelection(String librarySelection) {
        this.librarySelection = librarySelection;
    }

    public String getLibraryStrategy() {
        return libraryStrategy;
    }

    public void setLibraryStrategy(String libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public QualityScore getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(QualityScore qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Integer getPairingHorizon() {
        return pairingHorizon;
    }

    public void setPairingHorizon(Integer pairingHorizon) {
        this.pairingHorizon = pairingHorizon;
    }
}
