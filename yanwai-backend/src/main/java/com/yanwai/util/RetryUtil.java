package com.yanwai.util;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class RetryUtil {

    @Retryable(
            value = {TimeoutException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> T executeWithRetry(RetryCallback<T> callback) throws Exception {
        return callback.execute();
    }

    @FunctionalInterface
    public interface RetryCallback<T> {
        T execute() throws Exception;
    }

}
