package org.zalando.stups.fullstop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.stups.fullstop.config.RuleControllerProperties;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.web.api.NotFoundException;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class RuleControllerTest {

    public static final String MESSAGE = "No such Rule! Id: 12";
    private RuleDTO ruleDTO;

    private RuleEntity ruleEntity;


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private RuleEntityService ruleEntityService;

    @Autowired
    private TeamOperations teamOperationsMock;

    @Autowired
    private RuleControllerProperties ruleControllerPropertiesMock;


    @Before
    public void setUp() throws Exception {
        reset(ruleEntityService, teamOperationsMock, ruleControllerPropertiesMock);

        ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("12345");
        ruleDTO.setApplicationId("a-1234");
        ruleDTO.setApplicationVersion("1.0-SNAPSHOT");
        ruleDTO.setExpiryDate(DateTime.now());
        ruleDTO.setImageName("amiName");
        ruleDTO.setImageOwner("Peter Lustig");
        ruleDTO.setReason("BAM!");
        ruleDTO.setRegion("eu-west-1");
        ruleDTO.setViolationTypeEntityId("APPLICATION_NOT_PRESENT_IN_KIO");

        ruleEntity = new RuleEntity();
        ruleEntity.setId(1L);
        ruleEntity.setAccountId("1234");

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test-user", null));

        when(ruleEntityService.findAll()).thenReturn(newArrayList(ruleEntity));

        when(ruleControllerPropertiesMock.getAllowedTeams()).thenReturn(newArrayList("Owner", "OtherTeam"));
        when(teamOperationsMock.getAwsAccountsByUser(anyString())).thenReturn(newArrayList(
                new Account(
                        "Team",
                        "Foo",
                        "teams_account",
                        "Description",
                        "Owner",
                        false)));
        when(teamOperationsMock.getTeamIdsByUser(anyString())).thenReturn(newHashSet("Owner", "OtherTeamID"));

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();


    }


    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(ruleEntityService, teamOperationsMock, ruleControllerPropertiesMock);

    }

    @Test
    public void testShowWhitelistings() throws Exception {
        when(ruleEntityService.findAll()).thenReturn(newArrayList(ruleEntity));

        ResultActions resultActions = mockMvc.perform(get("/api/whitelisting-rules")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$[0].id").value(1));

        verify(ruleEntityService).findAll();
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();


    }

    @Test
    public void testAddWhitelisting() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("1234");
        when(ruleEntityService.save(any(RuleDTO.class))).thenReturn(ruleEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        String ruleAsJson = objectMapper.writeValueAsString(ruleDTO);

        ResultActions resultActions = mockMvc.perform(post("/api/whitelisting-rules").contentType(APPLICATION_JSON).content(ruleAsJson));
        resultActions.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.account_id").value("1234"));

        verify(ruleEntityService).save(any(RuleDTO.class));
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();


    }

    @Test
    public void testGetWhitelisting() throws Exception {
        when(ruleEntityService.findById(anyLong())).thenReturn(ruleEntity);

        ResultActions resultActions = mockMvc.perform(get("/api/whitelisting-rules/1")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.account_id").value("1234"));

        verify(ruleEntityService).findById(anyLong());
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testGetWhitelistingFails() throws Exception {
        when(ruleEntityService.findById(anyLong())).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(get("/api/whitelisting-rules/2")).andExpect(status().isNotFound());

        verify(ruleEntityService).findById(anyLong());
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testUpdateWhitelisting() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("4567");
        when(ruleEntityService.update(any(RuleDTO.class), anyLong())).thenReturn(ruleEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        String ruleAsJson = objectMapper.writeValueAsString(ruleDTO);

        ResultActions resultActions = mockMvc.perform(put("/api/whitelisting-rules/1").contentType(APPLICATION_JSON).content(ruleAsJson));
        resultActions.andExpect(status().isOk());

        verify(ruleEntityService).update(any(RuleDTO.class), anyLong());
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testUpdateWhitelistingFails() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("4567");
        when(ruleEntityService.update(any(RuleDTO.class), anyLong())).thenThrow(new NoSuchElementException(MESSAGE));

        ObjectMapper objectMapper = new ObjectMapper();
        String ruleAsJson = objectMapper.writeValueAsString(ruleDTO);

        ResultActions resultActions = mockMvc.perform(put("/api/whitelisting-rules/2").contentType(APPLICATION_JSON).content(ruleAsJson));

        verify(ruleEntityService).update(any(RuleDTO.class), anyLong());
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testInvalidUser() throws Exception {
        when(ruleControllerPropertiesMock.getAllowedTeams()).thenReturn(newArrayList("WrongTeam", "OtherTeam"));
        when(ruleEntityService.findAll()).thenReturn(newArrayList(ruleEntity));

        ResultActions resultActions = mockMvc.perform(get("/api/whitelisting-rules/")).andExpect(status().is4xxClientError());

        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testExpireWhitelistRuleSuccessfully() throws Exception {

        mockMvc.perform(delete("/api/whitelisting-rules/1")).andExpect(status().is2xxSuccessful());
        verify(ruleEntityService).expire(eq(1L), any(DateTime.class));
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();
    }

    @Test
    public void testExpireWhitelistRuleSuccessfullyWithParam() throws Exception {
        final DateTime param = DateTime.now().plusDays(1);
        mockMvc.perform(delete("/api/whitelisting-rules/1").param("expiryDate", param.toString(ISODateTimeFormat.dateTime()))).andExpect(status().is2xxSuccessful());
        verify(ruleEntityService).expire(eq(1L), any(DateTime.class));
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();
    }

    @Test
    public void testExpireWhitelistRuleSuccessfullyWithWrongParam() throws Exception {
        final DateTime param = DateTime.now().plusDays(1);
        mockMvc.perform(delete("/api/whitelisting-rules/1").param("expiryDate", param.toString(ISODateTimeFormat.basicDate()))).andExpect(status().is4xxClientError());
    }

    @Test
    public void testExpireWhitelistRuleFailsWithNoSuchElementException() throws Exception {
        doThrow(new NoSuchElementException()).when(ruleEntityService).expire(anyLong(), any(DateTime.class));

        mockMvc.perform(delete("/api/whitelisting-rules/1")).andExpect(status().is4xxClientError());

        verify(ruleEntityService).expire(eq(1L), any(DateTime.class));
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();
    }

    @Test
    public void testExpireWhitelistRuleFailsWithIllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException()).when(ruleEntityService).expire(anyLong(), any(DateTime.class));

        mockMvc.perform(delete("/api/whitelisting-rules/1")).andExpect(status().is4xxClientError());

        verify(ruleEntityService).expire(eq(1L), any(DateTime.class));
        verify(teamOperationsMock).getTeamIdsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();
    }

    @Configuration
    @Import(org.zalando.stups.fullstop.web.test.ControllerTestConfig.class)
    static class TestConfig {

        @Bean
        public RuleController whitelistController() {
            return new RuleController();
        }

        @Bean
        public RuleEntityRepository ruleEntityRepository() {
            return mock(RuleEntityRepository.class);
        }

        @Bean
        public RuleEntityService ruleEntityService() {
            return mock(RuleEntityService.class);
        }

        @Bean
        public TeamOperations teamOperationsMock() { return mock(TeamOperations.class); }

        @Bean
        public RuleControllerProperties ruleControllerPropertiesMock() { return mock(RuleControllerProperties.class); }


    }
}