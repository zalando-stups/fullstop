package org.zalando.stups.fullstop.swagger.api;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.zalando.stups.fullstop.s3.S3Service;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import org.zalando.stups.fullstop.web.test.RestControllerTestSupport;

import java.util.Base64;
import java.util.Date;

import static org.joda.time.DateTimeZone.UTC;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.stups.fullstop.s3.LogType.USER_DATA;

/**
 * Created by mrandi.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FullstopApiTest extends RestControllerTestSupport {

    public static final String ACCOUNT_ID = "123";

    public static final String ENCODED_LOG_FILE = Base64.getEncoder().encodeToString("this is my log".getBytes());

    public static final Date INSTANCE_BOOT_TIME = new DateTime(UTC).toDate();

    public static final String INSTANCE_ID = "i-123ds";

    public static final String REGION = "eu-west-1";

    @Autowired
    private FullstopApi fullstopApiController;

    @Autowired
    private ApplicationLifecycleService mockApplicationLifecycleService;

    private LogObj logObjRequest;

    @Before
    public void setUp() throws Exception {
        reset(mockApplicationLifecycleService);

        logObjRequest = new LogObj();
        logObjRequest.setAccountId(ACCOUNT_ID);
        logObjRequest.setLogData(ENCODED_LOG_FILE);
        logObjRequest.setInstanceBootTime(INSTANCE_BOOT_TIME);
        logObjRequest.setLogType(USER_DATA);
        logObjRequest.setInstanceId(INSTANCE_ID);
        logObjRequest.setRegion(REGION);

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test-user", null));
    }

    @Override
    protected void configure(StandaloneMockMvcBuilder mockMvcBuilder) {
        super.configure(mockMvcBuilder);
        mockMvcBuilder.alwaysDo(print());
    }

    @After
    public void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        verifyNoMoreInteractions(mockApplicationLifecycleService);
    }

    @Test
    public void testInstanceLogs() throws Exception {
        when(
                mockApplicationLifecycleService.saveInstanceLogLifecycle(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any())).thenReturn(new LifecycleEntity());

        byte[] bytes = objectMapper.writeValueAsBytes(logObjRequest);

        this.mockMvc.perform(
                post("/api/instance-logs").contentType(APPLICATION_JSON).content(bytes))
                    .andExpect(status().isCreated());

        verify(mockApplicationLifecycleService).saveInstanceLogLifecycle(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testInstanceLogsNotBase64LogDataEncoded() throws Exception {
        // test with not encoded log data
    }

    @Override
    protected Object[] mockMvcControllers() {
        return new Object[] { fullstopApiController };
    }

    @Configuration
    static class TestConfig {

        @Bean
        public FullstopApi fullstopApi() {
            return new FullstopApi();
        }

        @Bean
        public ApplicationLifecycleService applicationLifecycleService() {
            return mock(ApplicationLifecycleService.class);
        }

        @Bean
        public ViolationService violationService() {
            return mock(ViolationService.class);
        }

        @Bean
        public S3Service s3Writer() {
            return mock(S3Service.class);
        }

        @Bean
        public TeamOperations teamOperations() {
            return mock(TeamOperations.class);
        }
    }
}
