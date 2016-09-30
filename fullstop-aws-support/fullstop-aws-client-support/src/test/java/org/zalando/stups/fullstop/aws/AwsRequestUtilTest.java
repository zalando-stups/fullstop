package org.zalando.stups.fullstop.aws;

import com.amazonaws.AmazonServiceException;
import org.junit.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class AwsRequestUtilTest {

    @Test
    public void performRequestRetrySuccessfully() throws Exception {
        final Supplier<String> request = (Supplier<String>) mock(Supplier.class);
        when(request.get())
                .thenThrow(awsException("Throttling"))
                .thenThrow(awsException("RequestLimitExceeded"))
                .thenReturn("foo");

        assertThat(AwsRequestUtil.performRequest(request)).isEqualTo("foo");
    }

    @Test
    public void performRequestRetryFailed() throws Exception {
        final AmazonServiceException nonRetryableException = awsException("AnotherErrorCode");

        try {
            AwsRequestUtil.performRequest(() -> {
                throw nonRetryableException;
            });
        } catch (Exception e) {
            assertThat(e).isSameAs(nonRetryableException);
        }
    }

    @Test
    public void performRequestRetryFailed2() throws Exception {
        final RuntimeException nonRetryableException = new IllegalArgumentException();

        try {
            AwsRequestUtil.performRequest(() -> {
                throw nonRetryableException;
            });
        } catch (Exception e) {
            assertThat(e).isSameAs(nonRetryableException);
        }
    }

    @Test
    public void performRequestRetryExceeded() throws Exception {
        final Supplier<String> request = (Supplier<String>) mock(Supplier.class);
        final AmazonServiceException throttlingException = awsException("Throttling");
        final AmazonServiceException requestLimitExceededException = awsException("RequestLimitExceeded");
        when(request.get())
                .thenThrow(throttlingException)
                .thenThrow(requestLimitExceededException)
                .thenThrow(throttlingException)
                .thenThrow(requestLimitExceededException)
                .thenThrow(requestLimitExceededException);

        try {
            AwsRequestUtil.performRequest(request);
        } catch (AmazonServiceException e) {
            assertThat(e).isSameAs(requestLimitExceededException);
        }
    }

    private static AmazonServiceException awsException(final String errorCode) {
        return new AmazonServiceException("Exception occurred") {
            {
                setErrorCode(errorCode);
            }
        };
    }

}
