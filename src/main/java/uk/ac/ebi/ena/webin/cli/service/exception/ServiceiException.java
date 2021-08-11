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
package uk.ac.ebi.ena.webin.cli.service.exception;

public class
ServiceiException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public enum ErrorType {
        USER_ERROR("user error"),
        SYSTEM_ERROR("system error"),
        VALIDATION_ERROR("validation error");

        public final String text;

        ErrorType(String text) {
            this.text = text;
        }
    }

    private final ErrorType errorType;

    private ServiceiException(ErrorType errorType, Exception ex, String ... messages) {
        super(join(messages), ex);
        this.errorType = errorType;
    }

    private ServiceiException(ErrorType errorType, String ... messages) {
        super(join(messages));
        this.errorType = errorType;
    }

    private ServiceiException(ServiceiException ex, String ... messages) {
        super(join(ex.getMessage(), join(messages)), ex);
        this.errorType = ex.errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public static ServiceiException userError(Exception ex) {
        return userError(ex, ex.getMessage());
    }

    public static ServiceiException systemError(Exception ex) {
        return systemError(ex, ex.getMessage());
    }

    public static ServiceiException validationError(Exception ex) {
        return validationError(ex, ex.getMessage());
    }

    public static ServiceiException userError(Exception ex, String ... messages) {
        return new ServiceiException(ErrorType.USER_ERROR, ex, messages);
    }

    public static ServiceiException systemError(Exception ex, String ... messages) {
        return new ServiceiException(ErrorType.SYSTEM_ERROR, ex, messages);
    }

    public static ServiceiException validationError(Exception ex, String ... messages) {
        return new ServiceiException(ErrorType.VALIDATION_ERROR, ex, messages);
    }

    public static ServiceiException userError(String ... messages) {
        return new ServiceiException(ErrorType.USER_ERROR, messages);
    }

    public static ServiceiException systemError(String ... messages) {
        return new ServiceiException(ErrorType.SYSTEM_ERROR, messages);
    }

    public static ServiceiException validationError(String ... messages) {
        return new ServiceiException(ErrorType.VALIDATION_ERROR,messages);
    }

    public static ServiceiException error(ServiceiException ex, String ... messages) {
        return new ServiceiException(ex, messages);
    }

    private static String join(String ... messages) {
        String str = "";
        for (String msg : messages) {
            str += " " + msg;
        }
        return str.trim().replaceAll(" +", " ");
    }
}
