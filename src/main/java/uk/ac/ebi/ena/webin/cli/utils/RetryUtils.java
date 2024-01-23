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
package uk.ac.ebi.ena.webin.cli.utils;

import java.util.function.Consumer;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

public class RetryUtils {

    /**
     * Create a default retry template that does a total of 7 attempts (1 initial try + 6 failure retries) with wait times
     * of 1s before 1st retry, 3s before 2nd and then 5s before remaining retries when given errors occur.
     *
     * @param retryOnErrors
     * @return
     */
    public static RetryTemplate createDefaultRetryTemplate(Class<? extends Exception>... retryOnErrors) {
        RetryTemplateBuilder builder = RetryTemplate.builder()
            .maxAttempts(7)
            .exponentialBackoff(1000, 3, 5_000); // 1s, 3s, 5s, 5s, 5s, 5s

        if (retryOnErrors != null) {
            for (int i = 0; i < retryOnErrors.length; i++) {
                builder = builder.retryOn(retryOnErrors[i]);
            }
        }

        return builder.build();
    }

    /**
     *
     * @param retryCallback
     * @param beforeRetryCallback - Invoked before every retry attempt. This does not include the first attempt.
     * @param retryOnErrors
     * @return
     * @param <T>
     * @param <E>
     * @throws E
     */
    public static <T, E extends Throwable> T executeWithRetry(
        RetryCallback<T, E> retryCallback,
        Consumer<RetryContext> beforeRetryCallback,
        Class<? extends Exception>... retryOnErrors) throws E {

        RetryTemplate retryTemplate = createDefaultRetryTemplate(retryOnErrors);

        return retryTemplate.execute(ctx -> {
            if (ctx.getRetryCount() > 0) {
                beforeRetryCallback.accept(ctx);
            }

            return retryCallback.doWithRetry(ctx);
        });
    }
}
