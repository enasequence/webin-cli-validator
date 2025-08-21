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
package uk.ac.ebi.ena.webin.cli.service.exception;

import uk.ac.ebi.ena.webin.cli.validator.message.source.MessageFormatSource;

public enum ServiceMessage implements MessageFormatSource {
  SAMPLE_SERVICE_VALIDATION_ERROR(
      "Unknown sample {0} or the sample cannot be referenced by your submission account. Samples must be submitted before they can be referenced in the submission.");

  private final String text;

  ServiceMessage(String text) {
    this.text = text;
  }

  public String text() {
    return text;
  }
}
