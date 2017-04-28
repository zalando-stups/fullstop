package org.zalando.stups.fullstop.it;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.stups.fullstop.violation.EmbeddedPostgresJpaConfig;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.violation.service.impl.ApplicationLifecycleServiceImpl;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationLifecycleServiceImplIt.TestConfig.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Makes test slower
public class ApplicationLifecycleServiceImplIt {

    private static final String ACCOUNT_ID = "111222333444";
    private static final String REGION = "eu-west-1";
    private static final String INSTANCE_ID = "i1";
    private static final DateTime INSTANCE_BOOT_TIME = now();
    public static final String RUN_INSTANCES = "RunInstances";
    public static final String TERMINATE_INSTANCES = "TerminateInstances";

    @Autowired
    private ApplicationLifecycleService applicationLifecycleService;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    private ApplicationEntity application;

    private VersionEntity version;

    @Before
    public void setUp() throws Exception {
        application = new ApplicationEntity("myApp");
        version = new VersionEntity("0.1-SNAPSHOT");
    }

    @Test
    public void testNoOverwriteSame() throws Exception {
        final LifecycleEntity taupgelifecycleEntity = new LifecycleEntity();
        taupgelifecycleEntity.setInstanceId(INSTANCE_ID);
        taupgelifecycleEntity.setAccountId(ACCOUNT_ID);
        taupgelifecycleEntity.setRegion(REGION);
        taupgelifecycleEntity.setInstanceBootTime(INSTANCE_BOOT_TIME);

        final LifecycleEntity runlifecycleEntity = new LifecycleEntity();
        runlifecycleEntity.setInstanceId(INSTANCE_ID);
        runlifecycleEntity.setAccountId(ACCOUNT_ID);
        runlifecycleEntity.setRegion(REGION);
        runlifecycleEntity.setEventType(RUN_INSTANCES);

        final LifecycleEntity terminatelifecycleEntity = new LifecycleEntity();
        terminatelifecycleEntity.setInstanceId(INSTANCE_ID);
        terminatelifecycleEntity.setAccountId(ACCOUNT_ID);
        terminatelifecycleEntity.setRegion(REGION);
        terminatelifecycleEntity.setEventType(TERMINATE_INSTANCES);

        applicationLifecycleService.saveLifecycle(application, version, taupgelifecycleEntity);
        applicationLifecycleService.saveLifecycle(application, version, runlifecycleEntity);
        applicationLifecycleService.saveLifecycle(application, version, terminatelifecycleEntity);

        assertThat(lifecycleRepository.findAll().size()).isEqualTo(3);
    }

    @Test
    public void testNoOverwriteDifferent() throws Exception {
        final LifecycleEntity taupgelifecycleEntity = new LifecycleEntity();
        taupgelifecycleEntity.setInstanceId("97987");
        taupgelifecycleEntity.setAccountId(ACCOUNT_ID);
        taupgelifecycleEntity.setRegion(REGION);
        taupgelifecycleEntity.setInstanceBootTime(INSTANCE_BOOT_TIME);

        final LifecycleEntity runlifecycleEntity = new LifecycleEntity();
        runlifecycleEntity.setInstanceId(INSTANCE_ID);
        runlifecycleEntity.setAccountId(ACCOUNT_ID);
        runlifecycleEntity.setRegion(REGION);
        runlifecycleEntity.setEventType(RUN_INSTANCES);

        final LifecycleEntity terminatelifecycleEntity = new LifecycleEntity();
        terminatelifecycleEntity.setInstanceId("97987");
        terminatelifecycleEntity.setAccountId(ACCOUNT_ID);
        terminatelifecycleEntity.setRegion(REGION);
        terminatelifecycleEntity.setEventType(TERMINATE_INSTANCES);

        applicationLifecycleService.saveLifecycle(application, version, taupgelifecycleEntity);
        applicationLifecycleService.saveLifecycle(application, version, runlifecycleEntity);
        applicationLifecycleService.saveLifecycle(application, version, terminatelifecycleEntity);

        assertThat(lifecycleRepository.findAll().size()).isEqualTo(3);
    }


    @Configuration
    @Import(EmbeddedPostgresJpaConfig.class)
    static class TestConfig {
        @Bean
        ApplicationLifecycleService applicationLifecycleService() {
            return new ApplicationLifecycleServiceImpl();
        }

    }
}
