/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop.swagger.api;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.zalando.stups.fullstop.common.RestControllerTestSupport;
import org.zalando.stups.fullstop.s3.S3Writer;
import org.zalando.stups.fullstop.swagger.model.LogObj;
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

    private ViolationEntity violationResult;

    private LogObj logObjResult;

    @Before
    public void setUp() throws Exception {
        reset(violationServiceMock);

        violationResult = new ViolationEntity();
        violationResult.setAccountId(ACCOUNT_ID);
        violationResult.setMessage(MESSAGE);
        violationResult.setRegion(REGION);
        violationResult.setEventId(UUID.randomUUID().toString());

        logObjResult = new LogObj();
        logObjResult.setAccountId(ACCOUNT_ID);
        logObjResult.setLogData(ENCODED_LOG_FILE);
        logObjResult.setInstanceBootTime(INSTANCE_BOOT_TIME);
        logObjResult.setLogType(USER_DATA.toString());
        logObjResult.setInstanceId(INSTANCE_ID);
        logObjResult.setRegion(REGION);
    }

    @Test
    public void testAccountId() throws Exception {
        when(violationServiceMock.findAccountId()).thenReturn(Lists.newArrayList("123"));

        ResultActions resultActions = this.mockMvc.perform(get("/api/account-ids")).andExpect(status().isOk()).andDo(
                MockMvcResultHandlers.print());
        resultActions.andExpect(jsonPath("$").value(hasSize(1)));
    }

    @Test
    public void testAccountViolations() throws Exception {
        when(violationServiceMock.findByAccountId(any(String.class))).thenReturn(newArrayList(violationResult));

        ResultActions resultActions = this.mockMvc.perform(get("/api/account-violations/123"))
                                                  .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print());
        resultActions.andExpect(jsonPath("$").value(hasSize(1)));
    }

    @Test
    public void testInstanceLogs() throws Exception {

        byte[] bytes = objectMapper.writeValueAsBytes(logObjResult);

        this.mockMvc.perform(post("/api/instance-logs").contentType(MediaType.APPLICATION_JSON).content(bytes))
                    .andDo(MockMvcResultHandlers.print()).andExpect(status().isCreated()).andDo(MockMvcResultHandlers
                            .print());
    }

    @Test
    public void testInstanceLogsNotBase64LogDataEncoded() throws Exception {
        // test with not encoded log data
    }

    @Test
    @Ignore
    public void testViolations() throws Exception {
        when(violationServiceMock.findAll()).thenReturn(newArrayList(violationResult));

        ResultActions resultActions = this.mockMvc.perform(get("/api/violations")).andExpect(status().isOk()).andDo(
                MockMvcResultHandlers.print());
        resultActions.andExpect(jsonPath("$").value(hasSize(1)));
    }

    @Test
    public void testAcknowledgedViolations() throws Exception {

        violationResult.setChecked(true);
        violationResult.setComment("my comment");

        when(violationServiceMock.findOne(any(Integer.class))).thenReturn(violationResult);

        byte[] bytes = objectMapper.writeValueAsBytes(violationResult);

        this.mockMvc.perform(put("/api/violations/156").contentType(MediaType.APPLICATION_JSON).content(bytes))
                    .andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andDo(MockMvcResultHandlers
                            .print());
    }

    @Override
    protected Object[] mockMvcControllers() {
        return new Object[] {fullstopApiController};
    }

    @Configuration
    static class TestConfig {

        @Bean
        public FullstopApi apiApi() {
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
