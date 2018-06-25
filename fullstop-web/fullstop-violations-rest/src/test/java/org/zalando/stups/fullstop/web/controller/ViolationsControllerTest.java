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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import org.zalando.stups.fullstop.web.model.Violation;
import org.zalando.stups.fullstop.web.test.ControllerTestConfig;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.joda.time.DateTimeZone.UTC;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.stups.fullstop.web.test.MatcherHelper.hasSize;
import static org.zalando.stups.fullstop.web.test.TestDataInitializer.INITIALIZER;
import static org.zalando.stups.fullstop.web.test.builder.domain.ViolationEntityBuilder.violation;


@ContextConfiguration
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class ViolationsControllerTest {

    private static final String ACCOUNT_ID = "123";

    private static final String REGION = "eu-west-1";

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
    public void setUp() {
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
    public void tearDown() {
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

        this.mockMvc.perform(get("/api/violations/948439")).andExpect(status().isNotFound());
        verify(violationServiceMock).findOne(948439L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testViolations() throws Exception {
        when(violationServiceMock.queryViolations(any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), anyBoolean(), any(), any(), any())).thenReturn(
                new PageImpl<>(
                        newArrayList(violationResult), new PageRequest(0, 20, ASC, "id"), 50));

        final ResultActions resultActions = this.mockMvc.perform(get("/api/violations")).andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.content").value(hasSize(1)));

        verify(violationServiceMock).queryViolations(
                isNull(List.class),
                any(DateTime.class),
                any(DateTime.class),
                isNull(Long.class),
                anyBoolean(),
                isNull(Integer.class),
                isNull(Integer.class),
                isNull(Boolean.class),
                anyListOf(String.class),
                anyBoolean(),
                isNull(List.class),
                isNull(List.class),
                any());
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
    }

    @Test
    public void testViolationsWithParams() throws Exception {

        final DateTime dateTime = new DateTime(UTC);
        final long lastViolation = 0L;

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
                        any(),
                        anyBoolean(),
                        any(),
                        any(),
                        any()))
                .thenReturn(new PageImpl<>(newArrayList(violationResult), new PageRequest(0, 20, ASC, "id"), 50));

        final ResultActions resultActions = this.mockMvc.perform(
                get("/api/violations?accounts=123&checked=true&last-violation=0&since=" + dateTime))
                .andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.content").value(hasSize(1)));

        verify(violationServiceMock).queryViolations(
                eq(newArrayList("123")), any(DateTime.class), any(DateTime.class), eq(lastViolation), eq(
                        true), any(), any(), any(), anyListOf(String.class), anyBoolean(), anyListOf(String.class), anyListOf(String.class), any());
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
    }

    @Test
    public void testResolveViolation() throws Exception {
        when(violationServiceMock.findOne(anyLong())).thenReturn(violationResult);
        when(violationServiceMock.save(eq(violationResult))).thenReturn(violationResult);
        when(mockTeamOperations.getAwsAccountsByUser(anyString())).thenReturn(
                newArrayList(
                        new Account(
                                violationResult.getAccountId(),
                                "Foo",
                                "aws",
                                "account desc",
                                "account",
                                false)));

        violationRequest.setComment("my comment");

        final String message = "test";

        final byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(
                post("/api/violations/156/resolution").contentType(APPLICATION_JSON).content(bytes))
                .andExpect(status().isOk());

        verify(violationServiceMock).findOne(eq(156L));
        verify(violationServiceMock).save(eq(violationResult));
        verify(mockTeamOperations).getAwsAccountsByUser(eq("test-user"));
        verify(mockViolationConverter).convert(any(ViolationEntity.class));
    }

    @Test
    public void testResolveUnknownViolation() throws Exception {
        when(violationServiceMock.findOne(any(Long.class))).thenReturn(null);

        final String message = "test";

        final byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(
                post("/api/violations/156/resolution").contentType(APPLICATION_JSON).content(bytes))
                .andExpect(status().isNotFound());

        verify(violationServiceMock).findOne(eq(156L));
    }

    @Test
    public void testResolveOtherTeamsViolation() throws Exception {
        when(violationServiceMock.findOne(anyLong())).thenReturn(violationResult);
        when(mockTeamOperations.getAwsAccountsByUser(anyString())).thenReturn(
                newArrayList(
                        new Account(
                                "foo",
                                "Foo",
                                "other_teams_account",
                                "aws",
                                "account",
                                false)));

        violationRequest.setComment("my comment");

        final String message = "test";

        final byte[] bytes = objectMapper.writeValueAsBytes(message);

        this.mockMvc.perform(
                post("/api/violations/156/resolution").contentType(APPLICATION_JSON).content(bytes))
                .andExpect(status().isForbidden());

        verify(violationServiceMock).findOne(eq(156L));
        verify(mockTeamOperations).getAwsAccountsByUser(eq("test-user"));
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
        public Converter<ViolationEntity, Violation> violationConverter() {
            return mock(Converter.class, "violationConverter");
        }
    }
}
