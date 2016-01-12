package org.zalando.stups.fullstop.violation.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
        // somehow the @RunWith(SpringJUnit4ClassRunner.class) does not work well with @EnableRetry annotation
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
                // Last time succeed.
                .thenReturn(lifecycle);

        assertThat(service.saveLifecycle(application, version, lifecycle)).isSameAs(lifecycle);

        verify(mockApplicationRepository, times(3)).findByName(eq("foobar"));
        verify(mockVersionRepository, times(3)).findByName(eq("1.0"));
        verify(mockLifecycleRepository, times(3)).findByInstanceIdAndApplicationEntityAndVersionEntityAndRegion(anyString(), any(), any(), anyString());
        verify(mockLifecycleRepository, times(3)).save(any(LifecycleEntity.class));
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
