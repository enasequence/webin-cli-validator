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

/**
 * Generic response from any validator. Validator implementation should set proper status and set
 * all the validation messages in messageList using provided api.
 */
public class ValidationResponse {

  public enum status {
    VALIDATION_SUCCESS,
    VALIDATION_ERROR
  }

  private ValidationResponse.status validationStatus;

  public ValidationResponse() {}

  public ValidationResponse(ValidationResponse.status validationStatus) {
    this.validationStatus = validationStatus;
  }

  public void setStatus(ValidationResponse.status validationStatus) {
    this.validationStatus = validationStatus;
  }

  public ValidationResponse.status getStatus() {
    return this.validationStatus;
  }
}
