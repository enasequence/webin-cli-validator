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

import org.junit.Test;
import uk.ac.ebi.ena.webin.cli.validator.message.listener.MessageCounter;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageCounterTest {

    @Test
    public void testError() {
        MessageCounter textCounter = MessageCounter.text(ValidationMessage.Severity.ERROR, "TEST");
        MessageCounter regexCounter = MessageCounter.regex(ValidationMessage.Severity.ERROR, "TEST.*");
        ValidationResult result = ValidationResult.builder().listener(textCounter).listener(regexCounter).build();

        assertThat(textCounter.getCount()).isZero();
        assertThat(regexCounter.getCount()).isZero();

        result.add(new ValidationMessage(ValidationMessage.Severity.INFO, "TEST"));
        assertThat(textCounter.getCount()).isZero();
        assertThat(regexCounter.getCount()).isZero();

        result.add(new ValidationMessage(ValidationMessage.Severity.ERROR, "TEST"));
        assertThat(textCounter.getCount()).isOne();
        assertThat(regexCounter.getCount()).isOne();

        result.add(new ValidationMessage(ValidationMessage.Severity.ERROR, "TEST"));
        assertThat(textCounter.getCount()).isEqualTo(2);
        assertThat(regexCounter.getCount()).isEqualTo(2);

        result.add(new ValidationMessage(ValidationMessage.Severity.ERROR, "TEST1"));
        assertThat(textCounter.getCount()).isEqualTo(2);
        assertThat(regexCounter.getCount()).isEqualTo(3);

        result.add(new ValidationMessage(ValidationMessage.Severity.ERROR, "TEST2"));
        assertThat(textCounter.getCount()).isEqualTo(2);
        assertThat(regexCounter.getCount()).isEqualTo(4);
    }

    @Test
    public void testInfo() {
        MessageCounter textCounter = MessageCounter.text(ValidationMessage.Severity.INFO, "TEST");
        MessageCounter regexCounter = MessageCounter.regex(ValidationMessage.Severity.INFO, "TEST.*");
        ValidationResult result = ValidationResult.builder().listener(textCounter).listener(regexCounter).build();

        assertThat(textCounter.getCount()).isZero();
        assertThat(regexCounter.getCount()).isZero();

        result.add(new ValidationMessage(ValidationMessage.Severity.ERROR, "TEST"));
        assertThat(textCounter.getCount()).isZero();
        assertThat(regexCounter.getCount()).isZero();

        result.add(new ValidationMessage(ValidationMessage.Severity.INFO, "TEST"));
        assertThat(textCounter.getCount()).isOne();
        assertThat(regexCounter.getCount()).isOne();

        result.add(new ValidationMessage(ValidationMessage.Severity.INFO, "TEST"));
        assertThat(textCounter.getCount()).isEqualTo(2);
        assertThat(regexCounter.getCount()).isEqualTo(2);

        result.add(new ValidationMessage(ValidationMessage.Severity.INFO, "TEST1"));
        assertThat(textCounter.getCount()).isEqualTo(2);
        assertThat(regexCounter.getCount()).isEqualTo(3);

        result.add(new ValidationMessage(ValidationMessage.Severity.INFO, "TEST2"));
        assertThat(textCounter.getCount()).isEqualTo(2);
        assertThat(regexCounter.getCount()).isEqualTo(4);
    }

}
