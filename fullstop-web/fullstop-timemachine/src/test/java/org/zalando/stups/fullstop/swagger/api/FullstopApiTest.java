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
package org.zalando.stups.fullstop.swagger.api;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.zalando.stups.fullstop.common.RestControllerTestSupport;
import org.zalando.stups.fullstop.s3.S3Service;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationSeverity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import sun.misc.BASE64Encoder;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.joda.time.DateTimeZone.UTC;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.zalando.stups.fullstop.builder.domain.ViolationEntityBuilder.violation;
import static org.zalando.stups.fullstop.common.test.mvc.matcher.MatcherHelper.hasSize;
import static org.zalando.stups.fullstop.s3.LogType.USER_DATA;

/**
 * Created by mrandi.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FullstopApiTest extends RestControllerTestSupport {

    public static final String ACCOUNT_ID = "123";

    public static final String COMMENT = "my comment";

    public static final String ENCODED_LOG_FILE = new BASE64Encoder().encode("this is my log".getBytes());

    public static final Date INSTANCE_BOOT_TIME = new DateTime(UTC).toDate();

    public static final String INSTANCE_ID = "i-123ds";

    public static final String REGION = "eu-west-1";

    @Autowired
    private FullstopApi fullstopApiController;

    @Autowired
    private ViolationService violationServiceMock;

    @Autowired
    private TeamOperations mockTeamOperations;

    @Autowired
    private ApplicationLifecycleService mockApplicationLifecycleService;

    private Violation violationRequest;

    private LogObj logObjRequest;

    private ViolationEntity violationResult;

    @Before
    public void setUp() throws Exception {
        reset(violationServiceMock, mockTeamOperations, mockApplicationLifecycleService);

        violationRequest = new Violation();
        violationRequest.setAccountId(ACCOUNT_ID);
        violationRequest.setRegion(REGION);
        violationRequest.setEventId(UUID.randomUUID().toString());

        violationResult = testDataInitializer.create(violation().id(0L).version(0L));

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
        verifyNoMoreInteractions(violationServiceMock, mockTeamOperations, mockApplicationLifecycleService);
    }

    @Test
    public void testInstanceLogs() throws Exception {

        when(
                mockApplicationLifecycleService.saveInstanceLogLifecycle(
                        any(),
                        any(),
                        any(),
                        any(),
                        any())).thenReturn(new LifecycleEntity());

        byte[] bytes = objectMapper.writeValueAsBytes(logObjRequest);

        this.mockMvc.perform(
                post("/api/instance-logs").contentType(APPLICATION_JSON).content(bytes))
                    .andExpect(status().isCreated());

        verify(mockApplicationLifecycleService).saveInstanceLogLifecycle(any(), any(), any(), any(), any());
    }

    @Test
    public void testInstanceLogsNotBase64LogDataEncoded() throws Exception {
        // test with not encoded log data
    }

    @Test
    public void testGetOneViolation() throws Exception {
        violationResult.setId(1L);
        when(violationServiceMock.findOne(1L)).thenReturn(violationResult);

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations/1")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").value(1));
        verify(violationServiceMock).findOne(1L);
    }

    @Test
    public void testGetOneNullViolation() throws Exception {
        when(violationServiceMock.findOne(948439L)).thenReturn(null);

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations/948439"))
                                                        .andExpect(status().isNotFound());
        resultActions.andExpect(content().string("\"Violation with id: 948439 not found!\""));
        verify(violationServiceMock).findOne(948439L);
    }

    @Test
    public void testViolations() throws Exception {
        when(violationServiceMock.queryViolations(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                new PageImpl<>(
                        newArrayList(violationResult), new PageRequest(0, 20, ASC, "id"), 50));

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations")).andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.content").value(hasSize(1)));

        verify(violationServiceMock).queryViolations(
                isNull(List.class),
                isNull(DateTime.class),
                isNull(Long.class),
                isNull(Boolean.class),
                isNull(ViolationSeverity.class),
                isNull(Boolean.class),
                isNull(String.class),
                any());
    }

    @Test
    public void testViolationsWithParams() throws Exception {

        DateTime dateTime = new DateTime(UTC);
        long lastViolation = 0L;

        when(
                violationServiceMock.queryViolations(
                        eq(newArrayList("123")),
                        any(DateTime.class),
                        eq(lastViolation),
                        eq(true),
                        any(),
                        any(),
                        any(),
                        any()))
                .thenReturn(new PageImpl<>(newArrayList(violationResult), new PageRequest(0, 20, ASC, "id"), 50));

        ResultActions resultActions = this.mockMvc.perform(
                get("/api/violations?accounts=123&checked=true&last-violation=0&since=" + dateTime))
                                                  .andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.content").value(hasSize(1)));

        verify(violationServiceMock).queryViolations(
                eq(newArrayList("123")), any(DateTime.class), eq(lastViolation), eq(
                        true), any(), any(), any(), any());
    }

    @Test
    public void testResolveViolation() throws Exception {
        when(violationServiceMock.findOne(anyLong())).thenReturn(violationResult);
        when(violationServiceMock.save(eq(violationResult))).thenReturn(violationResult);
        when(mockTeamOperations.getTeamsByUser(anyString())).thenReturn(
                newArrayList(
                        new Account(
                                violationResult.getAccountId(),
                                "Foo",
                                "aws",
                                "account desc")));

        violationRequest.setComment("my comment");

        String message = "test";

        byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(
                post("/api/violations/156/resolution").contentType(APPLICATION_JSON).content(bytes))
                    .andExpect(status().isOk());

        verify(violationServiceMock).findOne(eq(156L));
        verify(violationServiceMock).save(eq(violationResult));
        verify(mockTeamOperations).getTeamsByUser(eq("test-user"));
    }

    @Test
    public void testResolveUnknownViolation() throws Exception {
        when(violationServiceMock.findOne(any(Long.class))).thenReturn(null);

        String message = "test";

        byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(
                post("/api/violations/156/resolution").contentType(APPLICATION_JSON).content(bytes))
                    .andExpect(status().isNotFound());

        verify(violationServiceMock).findOne(eq(156L));
    }

    @Test
    public void testResolveOtherTeamsViolation() throws Exception {
        when(violationServiceMock.findOne(anyLong())).thenReturn(violationResult);
        when(mockTeamOperations.getTeamsByUser(anyString())).thenReturn(
                newArrayList(
                        new Account(
                                "foo",
                                "Foo",
                                "other_teams_account",
                                "aws")));

        violationRequest.setComment("my comment");

        String message = "test";

        byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(
                post("/api/violations/156/resolution").contentType(APPLICATION_JSON).content(bytes))
                    .andExpect(status().isForbidden());

        verify(violationServiceMock).findOne(eq(156L));
        verify(mockTeamOperations).getTeamsByUser(eq("test-user"));
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
