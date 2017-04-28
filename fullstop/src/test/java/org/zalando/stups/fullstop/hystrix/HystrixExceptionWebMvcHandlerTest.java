package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static com.netflix.hystrix.exception.HystrixRuntimeException.FailureType.TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RunWith(SpringRunner.class)
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
