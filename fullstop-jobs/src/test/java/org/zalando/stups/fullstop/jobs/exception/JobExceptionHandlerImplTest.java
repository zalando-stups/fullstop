package org.zalando.stups.fullstop.jobs.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

public class JobExceptionHandlerImplTest {

    private JobExceptionHandlerImpl jobExceptionHandler;

    @Before
    public void setUp() throws Exception {
        jobExceptionHandler = new JobExceptionHandlerImpl();
    }

    @Test
    public void onException() throws Exception {
        jobExceptionHandler.onException(new IllegalArgumentException("Oops"), ImmutableMap.of("foo", "bar"));
    }

    @Test
    public void onRequestLimitExceededException() throws Exception {
        final AmazonServiceException exception = new AmazonServiceException("Oops");
        exception.setErrorCode("RequestLimitExceeded");
        jobExceptionHandler.onException(exception, ImmutableMap.of("aws_account", "111222333444"));
    }

    @Test
    public void onStsException() throws Exception {
        final AmazonServiceException  exception = new AWSSecurityTokenServiceException("bla");
        exception.setErrorCode("SomethingElse");
        jobExceptionHandler.onException(exception, ImmutableMap.of("aws_account", "111222333444"));
    }

    @Test
    public void onAmazonException() throws Exception {
        final AmazonServiceException  exception = new AmazonServiceException("bla");
        exception.setErrorCode("SomethingElse");
        jobExceptionHandler.onException(exception, ImmutableMap.of("aws_account", "111222333444"));
    }
}
