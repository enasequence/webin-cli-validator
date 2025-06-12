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

/** Manifest extension specific to sequence(template) context */
public class SequenceManifest extends Manifest<SequenceManifest.FileType> {

  public enum FileType {
    FLATFILE,
    FASTA,
    TAB,
    SAMPLE_TSV,
    TAX_TSV
  }

  private String analysisType;
  private String analysisProtocol;
  private String analysisDate;
  private String targetLocus;
  private String analysisCode;
  private String analysisVersion;
  private String organelle;
  private String forwardPrimerName;
  private String forwardPrimerSequence;
  private String reversePrimerName;
  private String reversePrimerSequence;
  private String analysisCenter;

  public String getAnalysisDate() {
    return analysisDate;
  }

  public void setAnalysisDate(String analysisDate) {
    this.analysisDate = analysisDate;
  }

  public String getTargetLocus() {
    return targetLocus;
  }

  public void setTargetLocus(String targetLocus) {
    this.targetLocus = targetLocus;
  }

  public String getAnalysisCode() {
    return analysisCode;
  }

  public void setAnalysisCode(String analysisCode) {
    this.analysisCode = analysisCode;
  }

  public String getAnalysisVersion() {
    return analysisVersion;
  }

  public void setAnalysisVersion(String analysisVersion) {
    this.analysisVersion = analysisVersion;
  }

  public String getOrganelle() {
    return organelle;
  }

  public void setOrganelle(String organelle) {
    this.organelle = organelle;
  }

  public String getForwardPrimerName() {
    return forwardPrimerName;
  }

  public void setForwardPrimerName(String forwardPrimerName) {
    this.forwardPrimerName = forwardPrimerName;
  }

  public String getForwardPrimerSequence() {
    return forwardPrimerSequence;
  }

  public void setForwardPrimerSequence(String forwardPrimerSequence) {
    this.forwardPrimerSequence = forwardPrimerSequence;
  }

  public String getReversePrimerName() {
    return reversePrimerName;
  }

  public void setReversePrimerName(String reversePrimerName) {
    this.reversePrimerName = reversePrimerName;
  }

  public String getReversePrimerSequence() {
    return reversePrimerSequence;
  }

  public void setReversePrimerSequence(String reversePrimerSequence) {
    this.reversePrimerSequence = reversePrimerSequence;
  }

  public String getAnalysisCenter() {
    return analysisCenter;
  }

  public void setAnalysisCenter(String analysisCenter) {
    this.analysisCenter = analysisCenter;
  }

  public String getAnalysisType() {
    return analysisType;
  }

  public void setAnalysisType(String analysisType) {
    this.analysisType = analysisType;
  }

  public String getAnalysisProtocol() {
    return analysisProtocol;
  }

  public void setAnalysisProtocol(String analysisProtocol) {
    this.analysisProtocol = analysisProtocol;
  }
}
