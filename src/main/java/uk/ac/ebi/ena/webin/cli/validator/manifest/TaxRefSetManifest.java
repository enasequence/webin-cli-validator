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

import java.util.*;

public class TaxRefSetManifest extends Manifest<TaxRefSetManifest.FileType> {
  public enum FileType {
    FASTA,
    TAB
  }

  private String taxonomySystem;
  private String taxonomySystemVersion;
  private Map<String, String> customFields = new LinkedHashMap<>();

  public String getTaxonomySystem() {
    return taxonomySystem;
  }

  public void setTaxonomySystem(String taxonomySystem) {
    this.taxonomySystem = taxonomySystem;
  }

  public String getTaxonomySystemVersion() {
    return taxonomySystemVersion;
  }

  public void setTaxonomySystemVersion(String taxonomySystemVersion) {
    this.taxonomySystemVersion = taxonomySystemVersion;
  }

  public Map<String, String> getCustomFields() {
    return customFields;
  }

  public void addCustomField(String key, String value) {
    this.customFields.put(key, value);
  }

  public void addCustomFields(Map<String, String> customFields) {
    this.customFields.putAll(customFields);
  }
}
