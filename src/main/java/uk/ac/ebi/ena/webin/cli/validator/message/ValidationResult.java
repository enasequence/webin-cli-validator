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
package uk.ac.ebi.ena.webin.cli.validator.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ena.webin.cli.validator.message.ValidationMessage.Severity;
import uk.ac.ebi.ena.webin.cli.validator.message.listener.MessageListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Writes validation messages into a report file or forwards them to a listener.
 */
public class ValidationResult implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ValidationResult.class);

    public static class Builder {
        private ValidationResult parentResult;
        private OutputStream strm;
        private boolean log;
        private final List<ValidationOrigin> origin = new ArrayList<>();
        private final List<MessageListener> listener = new ArrayList<>();

        public Builder() {
        }

        /**
         * Associates this validation result with a parent. Passes validation messages with validation origins
         * to the parent.
         *
         * @param parentResult the parent validation result
         * @return this builder
         */
        public Builder parent(ValidationResult parentResult) {
            this.parentResult = parentResult;
            return this;
        }

        /**
         * Writes validation messages to the report file.
         *
         * @param reportFile the report file
         * @return this builder
         */
        public Builder file(File reportFile) {
            try {
                this.strm = (reportFile != null) ?
                        Files.newOutputStream(
                                reportFile.toPath(),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND,
                                StandardOpenOption.SYNC) : null;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return this;
        }

        /**
         * Logs validation messages.
         *
         * @return this builder
         */
        public Builder log() {
            this.log = true;
            return this;
        }

        public Builder origin(ValidationOrigin origin) {
            if (origin != null) {
                this.origin.add(origin);
            }
            return this;
        }

        public Builder listener(MessageListener listener) {
            if (listener != null) {
                this.listener.add(listener);
            }
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(parentResult, strm, log, origin, listener);
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    private final ValidationResult parentResult;
    private final OutputStream strm;
    private final boolean log;
    private final List<ValidationOrigin> origin;
    private final List<MessageListener> listener;
    private AtomicInteger infoCount = new AtomicInteger();
    private AtomicInteger errorCount = new AtomicInteger();

    private ValidationResult(
            ValidationResult parentResult,
            OutputStream strm,
            boolean log,
            List<ValidationOrigin> origin,
            List<MessageListener> listener) {
        this.parentResult = parentResult;
        this.strm = strm;
        this.log = log;
        this.origin = origin;
        this.listener = listener;
    }

    /**
     * Adds a new validation message to the validation result.
     * The validation message will be included in all linked
     * validation results and will contain all the origins
     * associated with them.
     */
    public ValidationResult add(ValidationMessage message) {
        if (Severity.ERROR.equals(message.getSeverity())) {
            errorCount.incrementAndGet();
        } else {
            infoCount.incrementAndGet();
        }
        message.prependOrigin(origin);
        if (parentResult != null) {
            parentResult.add(message);
            // Delegate actions to the parent.
            return this;
        }
        listener.forEach(l -> l.listen(message));
        if (strm != null) {
            try {
                String str = formatForReport(message) + "\n";
                strm.write(str.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                if (!log) {
                    log(message);
                }
            }
        }
        if (log) {
            log(message);
        }
        return this;
    }

    private void log(ValidationMessage message) {
        try {
            String str = formatForLog(message);
            if (ValidationMessage.Severity.ERROR.equals(message.getSeverity())) {
                logger.error(str);
            } else {
                logger.info(str);
            }
        } catch (Exception ex) {
            // Do nothing
        }
    }

    static String formatForLog(ValidationMessage message) {
        String originStr = "";
        if (!message.getOrigin().isEmpty()) {
            originStr = " " + message.getOrigin().stream()
                    .map(origin -> origin.toString())
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        return String.format("%s%s",
                message.getMessage(),
                originStr);
    }

    static String formatForReport(ValidationMessage message) {
        return String.format("%s: %s", message.getSeverity(),
                formatForLog(message));
    }

    /**
     * Returns true if this validation result does not contain
     * any validation messages with ERROR severity.
     */
    public boolean isValid() {
        return errorCount.get() == 0;
    }

    /**
     * Returns the number of validation messages in this
     * validation result.
     */
    public long count() {
        return infoCount.get() + errorCount.get();
    }

    /**
     * Returns the number of validation messages in this
     * validation result for a given severity.
     */
    public long count(Severity severity) {
        if (Severity.ERROR.equals(severity)) {
            return errorCount.get();
        } else {
            return infoCount.get();
        }
    }

    @Override
    public void close() {
        if (strm != null) {
            try {
                strm.flush();
                strm.close();
            } catch (IOException ex) {
                // Do nothing
            }
        }
    }
}
