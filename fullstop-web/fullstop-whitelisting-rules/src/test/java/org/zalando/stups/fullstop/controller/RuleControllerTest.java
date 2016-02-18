package org.zalando.stups.fullstop.controller;

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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.config.RuleControllerProperties;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class RuleControllerTest {

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
        ruleDTO.setViolationTypeEntity("APPLICATION_NOT_PRESENT_IN_KIO");

        ruleEntity = new RuleEntity();
        ruleEntity.setId(1L);
        ruleEntity.setAccountId("1234");

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test-user", null));

        when(ruleEntityService.findAll()).thenReturn(newArrayList(ruleEntity));

        when(ruleControllerPropertiesMock.getAllowedTeams()).thenReturn(newArrayList("Team", "OtherTeam"));
        when(teamOperationsMock.getTeamsByUser(anyString())).thenReturn(newArrayList(
                new Account(
                        "Team",
                        "Foo",
                        "teams_account",
                        "aws",
                        "account",
                        false)));

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();


    }


    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(ruleEntityService, teamOperationsMock, ruleControllerPropertiesMock);
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testShowWhitelistings() throws Exception {
        when(ruleEntityService.findAll()).thenReturn(newArrayList(ruleEntity));

        ResultActions resultActions = mockMvc.perform(get("/whitelisting-rules/")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$[0].id").value(1));

        verify(ruleEntityService).findAll();
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();


    }

    @Test
    public void testAddWhitelisting() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("1234");
        when(ruleEntityService.save(any(RuleDTO.class))).thenReturn(ruleEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        String ruleAsJson = objectMapper.writeValueAsString(ruleDTO);

        ResultActions resultActions = mockMvc.perform(post("/whitelisting-rules/").contentType(APPLICATION_JSON).content(ruleAsJson));
        resultActions.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.account_id").value("1234"));

        verify(ruleEntityService).save(any(RuleDTO.class));
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();


    }

    @Test
    public void testGetWhitelisting() throws Exception {
        when(ruleEntityService.findById(anyLong())).thenReturn(ruleEntity);

        ResultActions resultActions = mockMvc.perform(get("/whitelisting-rules/1")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.account_id").value("1234"));

        verify(ruleEntityService).findById(anyLong());
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testGetWhitelistingFails() throws Exception {
        when(ruleEntityService.findById(anyLong())).thenReturn(null);

        ResultActions resultActions = mockMvc.perform(get("/whitelisting-rules/2")).andExpect(status().isNotFound());
        resultActions.andExpect(content().string("No such Rule! Id: 2"));

        verify(ruleEntityService).findById(anyLong());
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testUpdateWhitelisting() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("4567");
        when(ruleEntityService.update(any(RuleDTO.class), anyLong())).thenReturn(ruleEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        String ruleAsJson = objectMapper.writeValueAsString(ruleDTO);

        ResultActions resultActions = mockMvc.perform(put("/whitelisting-rules/1").contentType(APPLICATION_JSON).content(ruleAsJson));
        resultActions.andExpect(status().isOk());

        verify(ruleEntityService).update(any(RuleDTO.class), anyLong());
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();

    }

    @Test
    public void testUpdateWhitelistingFails() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("4567");
        when(ruleEntityService.update(any(RuleDTO.class), anyLong())).thenThrow(new NotFoundException("No such ID"));

        ObjectMapper objectMapper = new ObjectMapper();
        String ruleAsJson = objectMapper.writeValueAsString(ruleDTO);

        ResultActions resultActions = mockMvc.perform(put("/whitelisting-rules/2").contentType(APPLICATION_JSON).content(ruleAsJson));
        resultActions.andExpect(content().string("No such ID"));

        verify(ruleEntityService).update(any(RuleDTO.class), anyLong());
        verify(teamOperationsMock).getTeamsByUser(anyString());
        verify(ruleControllerPropertiesMock).getAllowedTeams();


    }

    @Test
    public void testInvalidUser() throws Exception {
        when(ruleControllerPropertiesMock.getAllowedTeams()).thenReturn(newArrayList("WrongTeam", "OtherTeam"));
        when(ruleEntityService.findAll()).thenReturn(newArrayList(ruleEntity));

        ResultActions resultActions = mockMvc.perform(get("/whitelisting-rules/")).andExpect(status().is4xxClientError());
        resultActions.andExpect(content().string("You don't have the permission to use this API"));

        verify(teamOperationsMock).getTeamsByUser(anyString());
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