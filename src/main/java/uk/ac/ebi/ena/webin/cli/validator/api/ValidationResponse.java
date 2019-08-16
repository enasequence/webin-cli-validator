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
package uk.ac.ebi.ena.webin.cli.validator.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic response from any validator
 */
public class ValidationResponse {

    public enum status {
        VALIDATION_SUCCESS,
        VALIDATION_ERROR
    }

    private ValidationResponse.status validationStatus;
    private List<String> messageList = new ArrayList<>();

    public ValidationResponse(ValidationResponse.status validationStatus) {
        this.validationStatus = validationStatus;
    }

    public ValidationResponse(ValidationResponse.status validationStatus, String message) {
        this.validationStatus = validationStatus;
        this.messageList.add(message);
    }

    public void setStatus(ValidationResponse.status validationStatus) {
        this.validationStatus = validationStatus;
    }

    public ValidationResponse.status getStatus() {
        return this.validationStatus;
    }

    public void addMessage(String message) {
        this.messageList.add(message);
    }

    public void addMessage(List<String> messages) {
        this.messageList.addAll(messages);
    }

    public String getFirstMessage() {
        return messageList.isEmpty() ? "" : messageList.get(0);
    }

    public List<String> getMessages() {
        return messageList;
    }
}
