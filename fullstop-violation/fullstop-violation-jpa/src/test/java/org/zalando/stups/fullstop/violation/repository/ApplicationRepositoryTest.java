package org.zalando.stups.fullstop.violation.repository;

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.EmbeddedPostgresJpaConfig;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EmbeddedPostgresJpaConfig.class)
@Transactional
public class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @PersistenceContext
    private EntityManager em;

    private ApplicationEntity application;

    private VersionEntity version1;

    private VersionEntity version2;

    private ApplicationEntity savedApplication;

    @Before
    public void setUp() throws Exception {
        // First version
        version1 = new VersionEntity();
        version1.setName("0.0.1");
        versionRepository.save(version1);
        // Second version
        version2 = new VersionEntity();
        version2.setName("0.9.3-SNAPSHOT");
        versionRepository.save(version2);


        // Build Application
        application = new ApplicationEntity();
        application.setName("Application");
        application.getVersionEntities().add(version1);
        application.getVersionEntities().add(version2);


        savedApplication = applicationRepository.save(application);
        em.flush();
        em.clear();
    }

    @Test
    public void testVersionsAreSaved() throws Exception {
        ApplicationEntity one = applicationRepository.findOne(savedApplication.getId());
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
        ApplicationEntity application = applicationRepository.findOne(savedApplication.getId());
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
}
