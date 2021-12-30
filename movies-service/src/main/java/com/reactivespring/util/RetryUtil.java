package com.reactivespring.util;

import java.time.Duration;

import com.reactivespring.exception.*;

import reactor.core.Exceptions;
import reactor.util.retry.Retry;

public class RetryUtil {

    private RetryUtil() {// just empty
    }

    public static Retry retrySpec() {
        return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof MoviesInfoServerException || ex instanceof ReviewsServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));
    }
}
