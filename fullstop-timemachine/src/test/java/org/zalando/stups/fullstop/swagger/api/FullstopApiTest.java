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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.zalando.stups.fullstop.common.RestControllerTestSupport;
import org.zalando.stups.fullstop.s3.S3Writer;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import sun.misc.BASE64Encoder;

import java.util.Date;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.joda.time.DateTimeZone.UTC;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.stups.fullstop.builder.domain.ViolationEntityBuilder.*;
import static org.zalando.stups.fullstop.common.test.mvc.matcher.MatcherHelper.hasSize;
import static org.zalando.stups.fullstop.s3.LogType.USER_DATA;

/**
 * Created by mrandi.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FullstopApiTest extends RestControllerTestSupport {

    public static final String ACCOUNT_ID = "123";
    public static final String MESSAGE = "my message";
    public static final String ENCODED_LOG_FILE = new BASE64Encoder().encode("this is my log".getBytes());
    public static final Date INSTANCE_BOOT_TIME = new DateTime(UTC).toDate();
    public static final String INSTANCE_ID = "i-123ds";
    public static final String REGION = "eu-west-1";

    @Autowired
    private FullstopApi fullstopApiController;

    @Autowired
    private ViolationService violationServiceMock;

    private Violation violationRequest;

    private LogObj logObjRequest;

    private ViolationEntity violationResult;

    @Before
    public void setUp() throws Exception {
        reset(violationServiceMock);

        violationRequest = new Violation();
        violationRequest.setAccountId(ACCOUNT_ID);
        violationRequest.setMessage(MESSAGE);
        violationRequest.setRegion(REGION);
        violationRequest.setEventId(UUID.randomUUID().toString());

        violationResult = testDataInitializer.create(violation()
                          .id(0L)
                          .version(0L));

        logObjRequest = new LogObj();
        logObjRequest.setAccountId(ACCOUNT_ID);
        logObjRequest.setLogData(ENCODED_LOG_FILE);
        logObjRequest.setInstanceBootTime(INSTANCE_BOOT_TIME);
        logObjRequest.setLogType(USER_DATA);
        logObjRequest.setInstanceId(INSTANCE_ID);
        logObjRequest.setRegion(REGION);
    }

    @Test
    public void testInstanceLogs() throws Exception {

        byte[] bytes = objectMapper.writeValueAsBytes(logObjRequest);

        this.mockMvc.perform(post("/api/instance-logs").contentType(MediaType.APPLICATION_JSON).content(bytes))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isCreated()).andDo(MockMvcResultHandlers
                .print());
    }

    @Test
    public void testInstanceLogsNotBase64LogDataEncoded() throws Exception {
        // test with not encoded log data
    }

    @Test
    public void testViolations() throws Exception {
        when(violationServiceMock.queryViolations(any(), any(),any(),any(),any())).thenReturn(new PageImpl<>(newArrayList
                (violationResult)));

        ResultActions resultActions = this.mockMvc.perform(get("/api/violations")).andExpect(status().isOk()).andDo(
                MockMvcResultHandlers.print());
        resultActions.andExpect(jsonPath("$").value(hasSize(1)));
    }

    @Test
    public void testResolutionViolation() throws Exception {

        violationRequest.setComment("my comment");

        when(violationServiceMock.findOne(any(Long.class))).thenReturn(violationResult);

        String message = "test";

        byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(post("/api/violations/156/resolution").contentType(MediaType.APPLICATION_JSON).content
                (bytes))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andDo(MockMvcResultHandlers
                .print());
    }

    @Override
    protected Object[] mockMvcControllers() {
        return new Object[]{fullstopApiController};
    }

    @Configuration
    static class TestConfig {

        @Bean
        public FullstopApi fullstopApi() {
            return new FullstopApi();
        }

        @Bean
        public ViolationService violationService() {
            return mock(ViolationService.class);
        }

        @Bean
        public S3Writer s3Writer() {
            return mock(S3Writer.class);
        }
    }
}
