package org.zalando.stups.fullstop.violation.service.impl;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.Assertions;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.util.Base64;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ApplicationLifecycleServiceImplTest {

    private static final String ACCOUNT_ID = "111222333444";
    private static final String REGION = "eu-west-1";
    private static final List<String> INSTANCE_IDS = asList("i1", "i2", "i3");
    private static final String INSTANCE_ID = "i1";
    private static final DateTime INSTANCE_BOOT_TIME = now();
    private static final String USERDATA_PATH = "path/to/the/logs";

    @Autowired
    private ApplicationRepository mockApplicationRepository;
    @Autowired
    private VersionRepository mockVersionRepository;
    @Autowired
    private LifecycleRepository mockLifecycleRepository;
    @Autowired
    @Qualifier(ApplicationLifecycleServiceImpl.BEAN_NAME)
    private ApplicationLifecycleService mockApplicationLifecycleService;

    @Autowired
    private ApplicationLifecycleService applicationLifecycleServiceImpl;

    private ApplicationEntity helloWorldApplication;

    private String applicationId = "fullstop";
    private String versionId = "1.0";
    private LifecycleEntity lifecycle;
    private ApplicationEntity application;
    private VersionEntity version;

    @Before
    public void setUp() throws Exception {
        reset(mockApplicationRepository, mockVersionRepository, mockLifecycleRepository, mockApplicationLifecycleService);

        helloWorldApplication = new ApplicationEntity("hello-world");
        when(mockApplicationRepository.findByInstanceIds(anyString(), anyString(), anyCollectionOf(String.class)))
                .thenReturn(helloWorldApplication);

        lifecycle = new LifecycleEntity();
        when(mockApplicationLifecycleService.saveLifecycle(any(), any(), any())).then(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            final ApplicationEntity applicationArg = (ApplicationEntity) args[0];
            final VersionEntity versionArg = (VersionEntity) args[1];
            final LifecycleEntity lifecycleArg = (LifecycleEntity) args[2];
            lifecycleArg.setApplicationEntity(applicationArg);
            lifecycleArg.setVersionEntity(versionArg);
            return lifecycleArg;
        });

        when(mockApplicationRepository.save(any(ApplicationEntity.class))).then(returnFirstArgument());
        when(mockVersionRepository.save(any(VersionEntity.class))).then(returnFirstArgument());
        when(mockLifecycleRepository.save(any(LifecycleEntity.class))).then(returnFirstArgument());

        application = new ApplicationEntity(applicationId);
        version = new VersionEntity(versionId);
    }

    private Answer<?> returnFirstArgument() {
        return invocationOnMock -> invocationOnMock.getArguments()[0];
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockApplicationRepository, mockVersionRepository, mockLifecycleRepository, mockApplicationLifecycleService);
    }

    @Test
    public void testSaveLifecycleWithNewAppAndVersion() throws Exception {
        final LifecycleEntity lifecycle = new LifecycleEntity();
        lifecycle.setInstanceId(INSTANCE_ID);
        lifecycle.setAccountId(ACCOUNT_ID);
        lifecycle.setRegion(REGION);

        final LifecycleEntity savedLifecycle = applicationLifecycleServiceImpl.saveLifecycle(
                application,
                version,
                lifecycle);

        verify(mockApplicationRepository).findByName(eq(applicationId));
        verify(mockVersionRepository).findByName(eq(versionId));
        verify(mockLifecycleRepository).findByInstanceIdAndApplicationEntityAndVersionEntityAndRegionAndAccountId(eq(INSTANCE_ID), eq(application), eq(version), eq(REGION), eq(ACCOUNT_ID));

        verify(mockApplicationRepository, times(2)).save(eq(application));
        verify(mockVersionRepository).save(eq(version));
        verify(mockLifecycleRepository).save(same(lifecycle));

        Assertions.assertThat(savedLifecycle).hasApplicationEntity(application);
        Assertions.assertThat(savedLifecycle).hasVersionEntity(version);
        Assertions.assertThat(savedLifecycle.getApplicationEntity()).hasOnlyVersionEntities(version);
    }

    @Test
    public void testSaveExistingLifecycle() throws Exception {
        final ApplicationEntity existingApp = new ApplicationEntity(applicationId);
        final VersionEntity existingVersion = new VersionEntity(versionId);
        existingApp.getVersionEntities().add(existingVersion);
        final LifecycleEntity existingLifecycle = new LifecycleEntity();

        when(mockApplicationRepository.findByName(eq(applicationId))).thenReturn(existingApp);
        when(mockVersionRepository.findByName(eq(versionId))).thenReturn(existingVersion);
        when(mockLifecycleRepository.findByInstanceIdAndApplicationEntityAndVersionEntityAndRegionAndAccountId(anyString(), any(), any(), anyString(), anyString()))
                .thenReturn(existingLifecycle);

        final LifecycleEntity lifecycle = new LifecycleEntity();
        lifecycle.setInstanceId(INSTANCE_ID);
        lifecycle.setAccountId(ACCOUNT_ID);
        lifecycle.setRegion(REGION);

        final LifecycleEntity savedLifecycle = applicationLifecycleServiceImpl.saveLifecycle(
                application,
                version,
                lifecycle);

        verify(mockApplicationRepository).findByName(eq(applicationId));
        verify(mockVersionRepository).findByName(eq(versionId));
        verify(mockLifecycleRepository).findByInstanceIdAndApplicationEntityAndVersionEntityAndRegionAndAccountId(eq(INSTANCE_ID), eq(application), eq(version), eq(REGION), eq(ACCOUNT_ID));
        verify(mockLifecycleRepository).save(same(existingLifecycle));

        Assertions.assertThat(savedLifecycle).isEqualTo(existingLifecycle);
    }


    @Test
    public void testSaveInstanceLogs() throws Exception {
        String userdata = encodeToBase64(
                "#taupage-ami-config\n"
                        + "application_id: '" + applicationId + "'\n"
                        + "application_version: '" + versionId + "'\n"
                        + "environment:\n"
                        + "  xx: xx");

        final LifecycleEntity result = applicationLifecycleServiceImpl.saveInstanceLogLifecycle(INSTANCE_ID, INSTANCE_BOOT_TIME, USERDATA_PATH, REGION, userdata, ACCOUNT_ID);
        Assertions.assertThat(result)
                .hasInstanceId(INSTANCE_ID)
                .hasInstanceBootTime(INSTANCE_BOOT_TIME)
                .hasUserdataPath(USERDATA_PATH)
                .hasAccountId(ACCOUNT_ID)
                .hasRegion(REGION)
                .hasApplicationEntity(new ApplicationEntity(applicationId))
                .hasVersionEntity(new VersionEntity(versionId));

        verify(mockApplicationLifecycleService).saveLifecycle(any(), any(), any());
    }

    @Test
    public void testSaveInstanceLogsMissingAppId() throws Exception {
        String userdata = encodeToBase64(
                "#taupage-ami-config\n"
                        + "application_version: '" + versionId + "'\n"
                        + "environment:\n"
                        + "  xx: xx");

        final LifecycleEntity result = applicationLifecycleServiceImpl.saveInstanceLogLifecycle("i1", now(), "path/to/the/logs", REGION, userdata, ACCOUNT_ID);
        assertThat(result).isNull();
    }

    @Test
    public void testSaveInstanceLogsMissingAppVersion() throws Exception {
        String userdata = encodeToBase64(
                "#taupage-ami-config\n"
                        + "application_id: '" + applicationId + "'\n"
                        + "environment:\n"
                        + "  xx: xx");

        final LifecycleEntity result = applicationLifecycleServiceImpl.saveInstanceLogLifecycle("i1", now(), "path/to/the/logs", REGION, userdata, ACCOUNT_ID);
        assertThat(result).isNull();
    }

    @Test
    public void testFindAppByInstanceIds() throws Exception {
        final ApplicationEntity result = applicationLifecycleServiceImpl.findAppByInstanceIds(ACCOUNT_ID, REGION, INSTANCE_IDS);

        assertThat(result).isEqualTo(helloWorldApplication);

        verify(mockApplicationRepository).findByInstanceIds(eq(ACCOUNT_ID), eq(REGION), eq(INSTANCE_IDS));
    }

    private static String encodeToBase64(String toEncode) {
        return Base64.getEncoder().encodeToString(
                (toEncode).getBytes());
    }

    @Configuration
    static class TestConfig {

        @Bean
        ApplicationLifecycleService applicationLifecycleServiceImpl() {
            return new ApplicationLifecycleServiceImpl();
        }

        @Bean(name = ApplicationLifecycleServiceImpl.BEAN_NAME)
        ApplicationLifecycleService mockApplicationLifecycleService() {
            return mock(ApplicationLifecycleService.class);
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
