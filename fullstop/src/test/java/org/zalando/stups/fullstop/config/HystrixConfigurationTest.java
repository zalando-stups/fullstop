package org.zalando.stups.fullstop.config;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class HystrixConfigurationTest {

    @Autowired
    private TestService testService;

    @Test(expected = RuntimeException.class)
    public void testMethodThrowsException() throws Exception {
        testService.throwException();
    }

    @Test(expected = HystrixRuntimeException.class)
    public void testMethodRunsIntoTimeout() throws Exception {
        final long start = System.currentTimeMillis();
        try {
            testService.runIntoTimeout();
        }
        finally {
            final long end = System.currentTimeMillis();
            // default timeout is 1 sec
            assertThat(end - start).isLessThan(1500);
        }
    }

    @Test
    public void testGetFallback() throws Exception {
        assertThat(testService.throwExceptionAndFallback()).isEqualTo("Hello Fallback!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadRequest() throws Exception {
        testService.badRequest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIgnoreException() throws Exception {
        testService.ignoreException();
    }

    @Configuration
    @Import(HystrixConfiguration.class)

    static class TestConfig {

        @Bean TestService testService() {
            return new TestService();
        }

    }

    static class TestService {

        @HystrixCommand()
        public void throwException() {
            throw new RuntimeException("this method always fails");
        }

        @HystrixCommand
        public void runIntoTimeout() throws InterruptedException {
            SECONDS.sleep(3000);
        }

        @HystrixCommand(ignoreExceptions = IllegalArgumentException.class)
        public void ignoreException() {
            throw new IllegalArgumentException("Oops");
        }

        @HystrixCommand(fallbackMethod = "fallback")
        public String throwExceptionAndFallback() {
            throw new RuntimeException("this method always fails");

        }

        @HystrixCommand(fallbackMethod = "fallback")
        public String badRequest() {
            // simulate failed input validation. Fallback won't be executed.
            throw new HystrixBadRequestException("Bla", new IllegalArgumentException("validation failed"));
        }

        public String fallback() {
            return "Hello Fallback!";
        }
    }

}
