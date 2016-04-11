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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
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
        reset(applicationLifecycleServiceMock);
        mockMvc = MockMvcBuilders.standaloneSetup(new LifecycleController(applicationLifecycleServiceMock))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(applicationLifecycleServiceMock);
    }

    @Test
    public void testFindByApplicationName() throws Exception {
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

        when(applicationLifecycleServiceMock.findByApplicationNameAndVersion(any(), any(), any())).thenReturn(
                new PageImpl<>(
                        lifecycleEntityList, new PageRequest(0, 20, ASC, "created"), 50));

        ResultActions resultActions = mockMvc.perform(get("/api/lifecycles/applications/test/versions")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.content", hasSize(2)));
        resultActions.andExpect(jsonPath("$.content[0].application").value("test"));
        resultActions.andExpect(jsonPath("$.content[1].version").value("2.0-SNAP"));

        verify(applicationLifecycleServiceMock).findByApplicationNameAndVersion(any(), any(), any());
    }

    @Test
    public void testFindByApplicationNameAndVersion() throws Exception {
        lifecycleEntity1 = new LifecycleEntity();
        lifecycleEntity1.setAccountId("456");
        lifecycleEntity1.setApplicationEntity(new ApplicationEntity("test"));
        lifecycleEntity1.setVersionEntity(new VersionEntity("2.0-SNAP"));
        lifecycleEntity1.setCreated(DateTime.now());
        List<LifecycleEntity> lifecycleEntityList = Lists.newArrayList(lifecycleEntity1);

        when(applicationLifecycleServiceMock.findByApplicationNameAndVersion(anyString(), anyString(), any())).thenReturn(
                new PageImpl<>(
                        lifecycleEntityList, new PageRequest(0, 20, ASC, "created"), 50));

        ResultActions resultActions = mockMvc.perform(get("/api/lifecycles/applications/test/versions/2.0-SNAP")).andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.content", hasSize(1)));
        resultActions.andExpect(jsonPath("$.content[0].application").value("test"));
        resultActions.andExpect(jsonPath("$.content[0].version").value("2.0-SNAP"));

        verify(applicationLifecycleServiceMock).findByApplicationNameAndVersion(anyString(), anyString(), any());
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ApplicationLifecycleService applicationLifecycleServiceMock() {
            return mock(ApplicationLifecycleService.class);
        }

    }
}