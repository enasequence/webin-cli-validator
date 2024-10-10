/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.ena.webin.cli.validator.manifest;

/** Manifest extension specific to genome context */
public class GenomeManifest extends Manifest<GenomeManifest.FileType> {

  public enum FileType {
    FASTA,
    FLATFILE,
    AGP,
    CHROMOSOME_LIST,
    UNLOCALISED_LIST
  }

  private String assemblyType;
  private String program;
  private String platform;
  private String moleculeType;
  private String coverage;
  private Integer minGapLength;
  private Boolean tpa;

  public String getAssemblyType() {
    return assemblyType;
  }

  public void setAssemblyType(String assemblyType) {
    this.assemblyType = assemblyType;
  }

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

  public String getMoleculeType() {
    return moleculeType;
  }

  public void setMoleculeType(String moleculeType) {
    this.moleculeType = moleculeType;
  }

  public String getCoverage() {
    return coverage;
  }

  public void setCoverage(String coverage) {
    this.coverage = coverage;
  }

  public Integer getMinGapLength() {
    return minGapLength;
  }

  public void setMinGapLength(Integer minGapLength) {
    this.minGapLength = minGapLength;
  }

  public boolean isTpa() {
    if (tpa == null) {
      return false;
    }
    return tpa;
  }

  public void setTpa(Boolean tpa) {
    this.tpa = tpa;
  }
}
