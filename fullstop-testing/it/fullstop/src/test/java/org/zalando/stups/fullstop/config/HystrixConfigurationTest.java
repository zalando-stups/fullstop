/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.config;

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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HystrixConfigurationTest {

    @Autowired
    private TestService testService;

    @Test
    public void testMethodThrowsException() throws Exception {
        try {
            testService.throwException();
            failBecauseExceptionWasNotThrown(HystrixRuntimeException.class);
        }
        catch (final HystrixRuntimeException e) {
            assertThat(e).hasRootCauseExactlyInstanceOf(RuntimeException.class);
        }
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

    @Configuration
    @Import(HystrixConfiguration.class)

    static class TestConfig {

        @Bean TestService testService() {
            return new TestService();
        }

    }

    static class TestService {

        @HystrixCommand
        public void throwException() {
            throw new RuntimeException("this method always fails");
        }

        @HystrixCommand
        public void runIntoTimeout() throws InterruptedException {
            SECONDS.sleep(3000);
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