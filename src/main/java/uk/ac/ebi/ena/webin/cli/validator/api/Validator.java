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
package uk.ac.ebi.ena.webin.cli.validator.api;

import uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest;

/**
 * Generic interface between webin-cli and validator, validator must implement this interface
 *
 * @param <T> any class extends Manifest
 */
public interface Validator<T extends Manifest, R extends ValidationResponse> {
  /**
   * @param manifest any class which extends uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest
   * @return ValidationResponse that contains the validation status and any returned information
   *     that affect submission XML creation
   */
  R validate(T manifest);
}
