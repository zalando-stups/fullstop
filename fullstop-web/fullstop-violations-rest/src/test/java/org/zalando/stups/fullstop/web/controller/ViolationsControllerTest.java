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
package org.zalando.stups.fullstop.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.fullstop.web.controller.ApiExceptionHandler;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import org.zalando.stups.fullstop.web.model.Violation;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.joda.time.DateTimeZone.UTC;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.zalando.stups.fullstop.web.test.MatcherHelper.hasSize;
import static org.zalando.stups.fullstop.web.test.TestDataInitializer.INITIALIZER;
import static org.zalando.stups.fullstop.web.test.builder.domain.ViolationEntityBuilder.violation;


@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ViolationsControllerTest {

    public static final String ACCOUNT_ID = "123";

    public static final String REGION = "eu-west-1";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ViolationService violationServiceMock;

    @Autowired
    private TeamOperations mockTeamOperations;

    @Autowired
    private Converter<ViolationEntity, Violation> mockViolationConverter;

    private Violation violationRequest;

    private ViolationEntity violationResult;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        reset(violationServiceMock, mockTeamOperations, mockViolationConverter);

        violationRequest = new Violation();
        violationRequest.setAccountId(ACCOUNT_ID);
        violationRequest.setRegion(REGION);
        violationRequest.setEventId(UUID.randomUUID().toString());

        violationResult = INITIALIZER.create(violation().id(0L).version(0L));

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test-user", null));

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();
        objectMapper = new ObjectMapper();

        when(mockViolationConverter.convert(any(ViolationEntity.class))).thenAnswer(invocationOnMock -> {
            final ViolationEntity entity = (ViolationEntity) invocationOnMock.getArguments()[0];
            final Violation dto = new Violation();
            dto.setId(entity.getId());
            return dto;
        });
    }

    @After
    public void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        verifyNoMoreInteractions(violationServiceMock, mockTeamOperations, mockViolationConverter);
    }

    @Test
    public void testGetOneViolation() throws Exception {
        violationResult.setId(1L);
        when(violationServiceMock.findOne(1L)).thenReturn(violationResult);

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations/1")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").value(1));
        verify(violationServiceMock).findOne(1L);
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
    }

    @Test
    public void testGetOneNullViolation() throws Exception {
        when(violationServiceMock.findOne(948439L)).thenReturn(null);

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations/948439"))
                .andExpect(status().isNotFound());
        resultActions.andExpect(content().string("Violation with id: 948439 not found!"));
        verify(violationServiceMock).findOne(948439L);
    }

    @Test
    public void testViolations() throws Exception {
        when(violationServiceMock.queryViolations(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                new PageImpl<>(
                        newArrayList(violationResult), new PageRequest(0, 20, ASC, "id"), 50));

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations")).andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.content").value(hasSize(1)));

        verify(violationServiceMock).queryViolations(
                isNull(List.class),
                any(DateTime.class),
                any(DateTime.class),
                isNull(Long.class),
                isNull(Boolean.class),
                isNull(Integer.class),
                isNull(Boolean.class),
                isNull(String.class),
                any());
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
    }

    @Test
    public void testViolationsWithParams() throws Exception {

        DateTime dateTime = new DateTime(UTC);
        long lastViolation = 0L;

        when(
                violationServiceMock.queryViolations(
                        eq(newArrayList("123")),
                        any(DateTime.class),
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
                eq(newArrayList("123")), any(DateTime.class), any(DateTime.class), eq(lastViolation), eq(
                        true), any(), any(), any(), any());
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
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
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
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

    @Configuration
    @Import(ControllerTestConfig.class)
    static class TestConfig {

        @Bean
        public ViolationsController violationsController() {
            return new ViolationsController();
        }

        @Bean
        public ApiExceptionHandler apiExceptionHandler() {
            return new ApiExceptionHandler();
        }

        @Bean
        public ViolationService violationService() {
            return mock(ViolationService.class);
        }

        @Bean
        public TeamOperations teamOperations() {
            return mock(TeamOperations.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        public Converter<ViolationEntity, Violation> violationConverter(){
            return mock(Converter.class, "violationConverter");
        }
    }
}
