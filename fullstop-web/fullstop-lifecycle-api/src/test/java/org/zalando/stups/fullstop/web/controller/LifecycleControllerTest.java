package org.zalando.stups.fullstop.web.controller;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class LifecycleControllerTest {

    @Autowired
    private ApplicationLifecycleService applicationLifecycleServiceMock;

    private LifecycleEntity lifecycleEntity1;
    private LifecycleEntity lifecycleEntity2;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        lifecycleEntity1 = new LifecycleEntity();
        lifecycleEntity1.setAccountId("1234");
        lifecycleEntity1.setApplicationEntity(new ApplicationEntity("test"));
        lifecycleEntity1.setVersionEntity(new VersionEntity("1.0-SNAP"));
        lifecycleEntity1.setCreated(DateTime.now());

        lifecycleEntity2 = new LifecycleEntity();
        lifecycleEntity2.setAccountId("456");
        lifecycleEntity2.setApplicationEntity(new ApplicationEntity("test"));
        lifecycleEntity2.setVersionEntity(new VersionEntity("2.0-SNAP"));
        lifecycleEntity2.setCreated(DateTime.now());
        List<LifecycleEntity> lifecycleEntityList = Lists.newArrayList(lifecycleEntity1, lifecycleEntity2);

        when(applicationLifecycleServiceMock.findByApplicationName(anyString())).thenReturn(lifecycleEntityList);

        mockMvc = MockMvcBuilders.standaloneSetup(new LifecycleController(applicationLifecycleServiceMock)).build();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(applicationLifecycleServiceMock);
    }

    @Test
    public void testFindByApplicationName() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/lifecycle/app/test")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$", hasSize(2)));
        resultActions.andExpect(jsonPath("$[0].application").value("test"));
        resultActions.andExpect(jsonPath("$[1].version").value("2.0-SNAP"));

        verify(applicationLifecycleServiceMock).findByApplicationName(anyString());
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ApplicationLifecycleService applicationLifecycleServiceMock() {
            return mock(ApplicationLifecycleService.class);
        }

    }
}