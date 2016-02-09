package org.zalando.stups.fullstop.controller;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class WhitelistControllerTest {

    private RuleDTO ruleDTO;

    private RuleEntity ruleEntity;


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private RuleEntityRepository ruleEntityRepositoryMock;


    @Before
    public void setUp() throws Exception {
        ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("12345");
        ruleDTO.setApplicationId("a-1234");
        ruleDTO.setApplicationVersion("1.0-SNAPSHOT");
        ruleDTO.setExpiryDate(LocalDate.now());
        ruleDTO.setImageName("amiName");
        ruleDTO.setImageOwner("Peter Lustig");
        ruleDTO.setReason("BAM!");
        ruleDTO.setRegion("eu-west-1");
        ruleDTO.setViolationTypeEntity("APPLICATION_NOT_PRESENT_IN_KIO");

        ruleEntity = new RuleEntity();
        ruleEntity.setId(1L);

        when(ruleEntityRepositoryMock.findAll()).thenReturn(Lists.newArrayList(ruleEntity));

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();


    }


    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(ruleEntityRepositoryMock);
    }

    @Test
    public void testShowWhitelistings() throws Exception {
        when(ruleEntityRepositoryMock.findAll()).thenReturn(Lists.newArrayList(ruleEntity));

        ResultActions resultActions = mockMvc.perform(get("/whitelisting-rules/")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$[0].id").value(1));

        verify(ruleEntityRepositoryMock).findAll();


    }

    @Test
    public void testAddWhitelisting() throws Exception {

    }

    @Test
    public void testGetWhitelisting() throws Exception {

    }

    @Test
    public void testUpdateWhitelisting() throws Exception {

    }

    @Configuration
    @Import(org.zalando.stups.fullstop.web.test.ControllerTestConfig.class)
    static class TestConfig {

        @Bean
        public WhitelistController whitelistController() { return new WhitelistController(); }

        @Bean
        public RuleEntityRepository ruleEntityRepository() { return mock(RuleEntityRepository.class); }


    }
}