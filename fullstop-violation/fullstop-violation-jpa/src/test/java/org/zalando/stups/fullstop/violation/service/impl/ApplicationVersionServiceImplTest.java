package org.zalando.stups.fullstop.violation.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.Stack;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationVersionService;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
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
        reset(applicationRepositoryMock, versionRepositoryMock);

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

        final Optional<Stack> stack = applicationVersionService.saveStack(MY_APP_1, SNAPSHOT);
        assertThat(stack).isPresent();
        assertThat(stack.map(Stack::getApplicationEntity).map(ApplicationEntity::getName)).isEqualTo(Optional.of(MY_APP_1));
        assertThat(stack.map(Stack::getVersionEntity).map(VersionEntity::getName)).isEqualTo(Optional.of(SNAPSHOT));

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

        final Optional<Stack> stack = applicationVersionService.saveStack(application.getName(), SNAPSHOT);

        assertThat(stack.map(Stack::getApplicationEntity).
                map(ApplicationEntity::getVersionEntities).
                map(List::size)).
                isEqualTo(Optional.of(3));

        verify(versionRepositoryMock).findByName(anyString());
        verify(versionRepositoryMock).save(any(VersionEntity.class));
        verify(applicationRepositoryMock).findByName(anyString());
        verify(applicationRepositoryMock).save(any(ApplicationEntity.class));
    }

    @Test
    public void testNullVersion() throws Exception {
        when(applicationRepositoryMock.findByName(anyString())).thenReturn(null);
        when(applicationRepositoryMock.save(any(ApplicationEntity.class))).thenReturn(applicationEntity);

        final Optional<Stack> stack = applicationVersionService.saveStack(MY_APP_1, null);
        assertThat(stack).isPresent();
        assertThat(stack.map(Stack::getApplicationEntity).map(ApplicationEntity::getName)).isEqualTo(Optional.of(MY_APP_1));
        assertThat(stack.map(Stack::getVersionEntity).map(VersionEntity::getName)).isEqualTo(Optional.empty());

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