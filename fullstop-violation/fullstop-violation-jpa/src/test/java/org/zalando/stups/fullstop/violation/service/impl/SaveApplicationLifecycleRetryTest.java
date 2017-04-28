package org.zalando.stups.fullstop.violation.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import javax.persistence.OptimisticLockException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SaveApplicationLifecycleRetryTest {

    private ApplicationLifecycleService service;

    private ApplicationRepository mockApplicationRepository;
    private VersionRepository mockVersionRepository;
    private LifecycleRepository mockLifecycleRepository;

    private ApplicationEntity application;
    private LifecycleEntity lifecycle;
    private VersionEntity version;

    @Before
    public void setUp() throws Exception {
        // somehow the @RunWith(SpringRunner.class) does not work well with @EnableRetry annotation
        // copied this approach to create a Spring context from
        // https://github.com/spring-projects/spring-retry/blob/master/src/test/java/org/springframework/retry/annotation/EnableRetryTests.java
        final ApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
        service = context.getBean(ApplicationLifecycleService.class);
        mockApplicationRepository = context.getBean(ApplicationRepository.class);
        mockVersionRepository = context.getBean(VersionRepository.class);
        mockLifecycleRepository = context.getBean(LifecycleRepository.class);

        reset(mockApplicationRepository, mockVersionRepository, mockLifecycleRepository);

        application = new ApplicationEntity("foobar");
        version = new VersionEntity("1.0");
        lifecycle = new LifecycleEntity();

        application.getVersionEntities().add(version);
        version.getApplicationEntities().add(application);

        when(mockApplicationRepository.findByName(anyString())).thenReturn(application);
        when(mockVersionRepository.findByName(anyString())).thenReturn(version);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockApplicationRepository, mockVersionRepository, mockLifecycleRepository);
    }

    @Test
    public void testRetry() throws Exception {
        when(mockLifecycleRepository.save(any(LifecycleEntity.class)))
                // first time throw an exception
                .thenThrow(new ObjectOptimisticLockingFailureException(ApplicationEntity.class, "foobar"))
                // second time throw another exception
                .thenThrow(new OptimisticLockException("Oops"))
                // third time thrwo another exception
                .thenThrow(new DataIntegrityViolationException("Hoppla"))
                // Last time succeed.
                .thenReturn(lifecycle);

        assertThat(service.saveLifecycle(application, version, lifecycle)).isSameAs(lifecycle);

        verify(mockApplicationRepository, times(4)).findByName(eq("foobar"));
        verify(mockVersionRepository, times(4)).findByName(eq("1.0"));

        verify(mockLifecycleRepository, times(4)).save(any(LifecycleEntity.class));
    }


    @Test(expected = DataIntegrityViolationException.class)
    public void testFailOnMaxAttemptsExceeded() throws Exception {
        when(mockLifecycleRepository.save(any(LifecycleEntity.class))).thenThrow(new DataIntegrityViolationException("constraint violation"));

        try {
            service.saveLifecycle(application, version, lifecycle);
        } finally {
            verify(mockApplicationRepository, times(10)).findByName(eq("foobar"));
            verify(mockVersionRepository, times(10)).findByName(eq("1.0"));

            verify(mockLifecycleRepository, times(10)).save(any(LifecycleEntity.class));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailOnNonRetriableException() throws Exception {
        when(mockLifecycleRepository.save(any(LifecycleEntity.class))).thenThrow(new IllegalArgumentException());

        try {
            service.saveLifecycle(application, version, lifecycle);
        } finally {
            verify(mockApplicationRepository).findByName(eq("foobar"));
            verify(mockVersionRepository).findByName(eq("1.0"));

            verify(mockLifecycleRepository).save(any(LifecycleEntity.class));
        }

    }

    @Configuration
    @EnableRetry(proxyTargetClass = true)
    static class TestConfig {

        @Bean
        ApplicationLifecycleService applicationLifecycleService() {
            return new ApplicationLifecycleServiceImpl();
        }

        @Bean
        ApplicationRepository applicationRepository() {
            return mock(ApplicationRepository.class);
        }

        @Bean
        VersionRepository versionRepository() {
            return mock(VersionRepository.class);
        }

        @Bean
        LifecycleRepository lifecycleRepository() {
            return mock(LifecycleRepository.class);
        }
    }
}
