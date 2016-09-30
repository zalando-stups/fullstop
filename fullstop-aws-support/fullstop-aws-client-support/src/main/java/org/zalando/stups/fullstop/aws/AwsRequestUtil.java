package org.zalando.stups.fullstop.aws;

import com.amazonaws.AmazonServiceException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonMap;

public final class AwsRequestUtil {

    private static final int MAX_ATTEMPTS = 10;

    private static final Set<String> RETRYABLE_ERROR_CODES = newHashSet("Throttling", "RequestLimitExceeded");

    private static final RetryTemplate RETRY_TEMPLATE;

    static {
        RETRY_TEMPLATE = new RetryTemplate();
        RETRY_TEMPLATE.setThrowLastExceptionOnExhausted(true);
        RETRY_TEMPLATE.setRetryPolicy(new SimpleRetryPolicy(MAX_ATTEMPTS, singletonMap(RetryableException.class, true)));
        RETRY_TEMPLATE.setBackOffPolicy(new ExponentialBackOffPolicy());
    }

    private static class RetryableException extends RuntimeException {
        RetryableException(RuntimeException cause) {
            super(cause);
        }
    }

    /**
     * Performs the request and retries it on Amazon rate limiting (with exponential backoff).
     *
     * @param request does usually contain an AmazonClient request, e.g. <code>() -> ec2Client.describeInstances()</code>
     * @return the result of the request.
     */
    public static <T> T performRequest(Supplier<T> request) {
        try {
            return RETRY_TEMPLATE.execute(
                    context -> {
                        try {
                            return request.get();
                        } catch (final AmazonServiceException e) {
                            if (RETRYABLE_ERROR_CODES.contains(e.getErrorCode())) {
                                throw new RetryableException(e);
                            } else {
                                throw e;
                            }
                        }
                    });
        } catch (RetryableException e) {
            throw ((RuntimeException) e.getCause());
        }
    }

    private AwsRequestUtil() {
    }
}
