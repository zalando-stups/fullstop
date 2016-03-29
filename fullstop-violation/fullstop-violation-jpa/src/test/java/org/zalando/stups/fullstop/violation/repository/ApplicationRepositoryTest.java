package org.zalando.stups.fullstop.violation.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.EmbeddedPostgresJpaConfig;
import org.zalando.stups.fullstop.violation.entity.AccountRegion;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EmbeddedPostgresJpaConfig.class)
@Transactional
public class ApplicationRepositoryTest {

    private static final String ACCOUNT01 = "account01";
    private static final String EU_WEST_1 = "eu-west-1";
    private static final String US_EAST_1 = "us-east-1";
    private static final String ACCOUNT02 = "account02";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    @PersistenceContext
    private EntityManager em;

    private ApplicationEntity application;

    private VersionEntity version1;

    private VersionEntity version2;

    @Before
    public void setUp() throws Exception {
        // First version
        version1 = new VersionEntity();
        version1.setName("1");
        version1 = versionRepository.save(version1);
        // Second version
        version2 = new VersionEntity();
        version2.setName("0.9.3-SNAPSHOT");
        version2 = versionRepository.save(version2);

        final VersionEntity version001 = versionRepository.save(new VersionEntity("0.0.1"));
        final VersionEntity version002 = versionRepository.save(new VersionEntity("0.0.2"));

        // Build Application
        application = new ApplicationEntity();
        application.setName("Application");
        application.getVersionEntities().add(version1);
        application.getVersionEntities().add(version2);
        application = applicationRepository.save(application);

        final ApplicationEntity anotherApplication = applicationRepository.save(new ApplicationEntity("anotherApplication"));

        final LifecycleEntity lifecycle1 = new LifecycleEntity();
        lifecycle1.setAccountId(ACCOUNT01);
        lifecycle1.setRegion(EU_WEST_1);
        lifecycle1.setApplicationEntity(application);
        lifecycle1.setVersionEntity(version001);
        lifecycleRepository.save(lifecycle1);

        final LifecycleEntity lifecycle2 = new LifecycleEntity();
        lifecycle2.setAccountId(ACCOUNT01);
        lifecycle2.setRegion(EU_WEST_1);
        lifecycle2.setApplicationEntity(application);
        lifecycle2.setVersionEntity(version002);
        lifecycleRepository.save(lifecycle2);

        final LifecycleEntity lifecycle3 = new LifecycleEntity();
        lifecycle3.setAccountId(ACCOUNT01);
        lifecycle3.setRegion(US_EAST_1);
        lifecycle3.setApplicationEntity(application);
        lifecycle3.setVersionEntity(version001);
        lifecycleRepository.save(lifecycle3);

        final LifecycleEntity lifecycle4 = new LifecycleEntity();
        lifecycle4.setAccountId(ACCOUNT02);
        lifecycle4.setRegion(EU_WEST_1);
        lifecycle4.setApplicationEntity(application);
        lifecycle4.setVersionEntity(version001);
        lifecycleRepository.save(lifecycle4);

        final LifecycleEntity lifecycle5 = new LifecycleEntity();
        lifecycle5.setAccountId(ACCOUNT02);
        lifecycle5.setRegion(US_EAST_1);
        lifecycle5.setApplicationEntity(anotherApplication);
        lifecycle5.setVersionEntity(version001);
        lifecycleRepository.save(lifecycle5);

        em.flush();
        em.clear();
    }

    @Test
    public void testVersionsAreSaved() throws Exception {
        ApplicationEntity one = applicationRepository.findOne(application.getId());
        assertThat(one.getVersionEntities()).isNotEmpty();
    }

    @Test
    public void testManyToMany() throws IOException {
        List<ApplicationEntity> applications = applicationRepository.findAll();
        assertThat(applications).isNotNull();
        assertThat(applications).isNotEmpty();
    }

    @Test
    public void testAppHasMultipleVersions() throws Exception {
        ApplicationEntity application = applicationRepository.findOne(this.application.getId());
        List<VersionEntity> appVersions = application.getVersionEntities();
        assertThat(appVersions.size()).isEqualTo(2);
    }

    @Test
    public void testVersionsHaveSameApp() throws Exception {
        List<VersionEntity> versionEntities = versionRepository.findAll();
        List<ApplicationEntity> apps1 = versionEntities.get(0).getApplicationEntities();
        List<ApplicationEntity> apps2 = versionEntities.get(1).getApplicationEntities();
        assertThat(apps1.get(0)).isEqualTo(apps2.get(0));
    }

    @Test
    public void testGetDeployments() throws Exception {
        assertThat(applicationRepository.findDeployments(application.getName()))
                .containsOnly(
                        new AccountRegion(ACCOUNT01, EU_WEST_1),
                        new AccountRegion(ACCOUNT01, US_EAST_1),
                        new AccountRegion(ACCOUNT02, EU_WEST_1));
    }
}
