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

import java.util.LinkedHashMap;
import java.util.Map;

/** Manifest extension specific to annotation context */
public class AnnotationManifest extends Manifest<AnnotationManifest.FileType> {

  public enum FileType {
    GFF3
  }

  private String analysisType;
  private Map<String, String> attributes = new LinkedHashMap<>();

  public String getAnalysisType() {
    return analysisType;
  }

  public void setAnalysisType(String analysisType) {
    this.analysisType = analysisType;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void addAttribute(String tag, String value) {
    this.attributes.put(tag, value);
  }

  public void addAttributes(Map<String, String> attributes) {
    this.attributes.putAll(attributes);
  }
}
