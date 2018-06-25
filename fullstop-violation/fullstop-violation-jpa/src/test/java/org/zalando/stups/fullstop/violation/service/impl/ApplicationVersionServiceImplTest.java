package org.zalando.stups.fullstop.violation.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.Stack;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationVersionService;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class ApplicationVersionServiceImplTest {

    public static final String SNAPSHOT = "1.0-SNAPSHOT";
    public static final String MY_APP_1 = "myApp1";
    @Autowired
    private ApplicationVersionService applicationVersionService;

    @Autowired
    private ApplicationRepository applicationRepositoryMock;

    @Autowired
    private VersionRepository versionRepositoryMock;

    private ApplicationEntity applicationEntity;

    private VersionEntity versionEntity;

    @Before
    public void setUp() throws Exception {
        reset(applicationRepositoryMock);
        reset(versionRepositoryMock);

        applicationEntity = new ApplicationEntity(MY_APP_1);
        versionEntity = new VersionEntity(SNAPSHOT);


        when(versionRepositoryMock.findByName(anyString())).thenReturn(null);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(applicationRepositoryMock, versionRepositoryMock);
    }

    @Test
    public void testSaveStack() throws Exception {
        when(applicationRepositoryMock.findByName(anyString())).thenReturn(null);
        when(applicationRepositoryMock.save(any(ApplicationEntity.class))).thenReturn(applicationEntity);
        when(versionRepositoryMock.save(any(VersionEntity.class))).thenReturn(versionEntity);

        final Stack stack = applicationVersionService.saveStack(MY_APP_1, SNAPSHOT);
        assertThat(stack.getApplicationEntity().getName()).isEqualTo(MY_APP_1);
        assertThat(stack.getVersionEntity().getName()).isEqualTo(SNAPSHOT);

        verify(versionRepositoryMock).findByName(anyString());
        verify(versionRepositoryMock).save(any(VersionEntity.class));
        verify(applicationRepositoryMock).findByName(anyString());
        verify(applicationRepositoryMock).save(any(ApplicationEntity.class));

    }

    @Test
    public void testVersionAdding() throws Exception {
        final VersionEntity version1 = new VersionEntity("1.0");
        final VersionEntity version2 = new VersionEntity("2.0");
        final ApplicationEntity application = new ApplicationEntity("WowApp");
        application.setVersionEntities(newArrayList(version1,version2));

        when(applicationRepositoryMock.findByName(anyString())).thenReturn(application);
        when(applicationRepositoryMock.save(any(ApplicationEntity.class))).thenReturn(application);
        when(versionRepositoryMock.save(any(VersionEntity.class))).thenReturn(versionEntity);

        final Stack stack = applicationVersionService.saveStack(application.getName(), SNAPSHOT);

        assertThat(stack.getApplicationEntity().getVersionEntities().size()).isEqualTo(3);

        verify(versionRepositoryMock).findByName(anyString());
        verify(versionRepositoryMock).save(any(VersionEntity.class));
        verify(applicationRepositoryMock).findByName(anyString());
        verify(applicationRepositoryMock).save(any(ApplicationEntity.class));
    }

    @Test
    public void testNullVersion() throws Exception {
        when(applicationRepositoryMock.findByName(anyString())).thenReturn(null);
        when(applicationRepositoryMock.save(any(ApplicationEntity.class))).thenReturn(applicationEntity);

        final Stack stack = applicationVersionService.saveStack(MY_APP_1, null);
        assertThat(stack.getApplicationEntity().getName()).isEqualTo(MY_APP_1);
        assertThat(stack.getVersionEntity()).isEqualTo(null);

        verify(applicationRepositoryMock).findByName(anyString());
        verify(applicationRepositoryMock).save(any(ApplicationEntity.class));

    }

    @Configuration
    static class TestConfig {

        @Bean
        ApplicationVersionService applicationVersionService() {
            return new ApplicationVersionServiceImpl();
        }

        @Bean
        ApplicationRepository applicationRepositoryMock() {
            return mock(ApplicationRepository.class);
        }

        @Bean
        VersionRepository versionRepositoryMock() {
            return mock(VersionRepository.class);
        }
    }
}
