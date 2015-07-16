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
package org.zalando.stups.fullstop.hystrix;

import static com.netflix.hystrix.exception.HystrixRuntimeException.FailureType.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class HystrixExceptionWebMvcHandlerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = webAppContextSetup(wac).build();
    }

    @Test
    public void testHystrixException() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(get("/throw-hystrix-exception"))
                                           .andDo(print())
                                           .andExpect(status().is(503))
                                           .andReturn();
        assertThat(mvcResult.getResponse().getErrorMessage()).isEqualTo("Dependency unavailable");
    }

    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean HystrixExceptionWebMvcHandler hystrixExceptionWebMvcHandler() {
            return new HystrixExceptionWebMvcHandler();
        }

        @Bean TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {

        @RequestMapping(method = GET, value = "/throw-hystrix-exception") void test() {
            throw new HystrixRuntimeException(
                    TIMEOUT,
                    null,
                    "Oops, something went wrong",
                    new IllegalStateException("fatal error"),
                    null);
        }
    }
}