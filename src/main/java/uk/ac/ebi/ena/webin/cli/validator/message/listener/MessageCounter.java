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
package uk.ac.ebi.ena.webin.cli.validator.message.listener;

import java.util.function.Predicate;

import uk.ac.ebi.ena.webin.cli.validator.message.ValidationMessage;
import uk.ac.ebi.ena.webin.cli.validator.message.ValidationMessage.Severity;

public class MessageCounter implements MessageListener {

    private final Severity severity;
    private final Predicate<String> messagePredicate;
    private int count = 0;

    public MessageCounter(Severity severity, Predicate<String> messagePredicate) {
        this.severity = severity;
        this.messagePredicate = messagePredicate;
    }

    @Override
    public void listen(ValidationMessage message) {
        if (severity.equals(message.getSeverity()) &&
                messagePredicate.test(message.getMessage())) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }

    public static MessageCounter text(Severity severity, String text) {
        return new MessageCounter(severity, m -> m.equals(text));
    }

    public static MessageCounter regex(Severity severity, String regex) {
        return new MessageCounter(severity, m -> m.matches(regex));
    }
}
