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
package uk.ac.ebi.ena.webin.cli.validator.reference;

import java.util.Set;

/** Reference attribute with tag, value and unit. */
public class Attribute {

  private final String name;
  private final String value;
  private final Set<String> iri;
  private final String unit;
  private final String tag;

  public Attribute(String name, String value, Set<String> iri, String unit, String tag) {
    this.name = name;
    this.value = value;
    this.iri = iri;
    this.unit = unit;
    this.tag = tag;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public Set<String> getIri() {
    return iri;
  }

  public String getUnit() {
    return unit;
  }

  public String getTag() {
    return tag;
  }
}
