package org.zalando.stups.fullstop.violation.repository;

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL;
import junit.framework.TestCase;
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
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.ApplicationVersionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gkneitschel.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationRepositoryTest.TestConfig.class)
@Transactional
public class ApplicationRepositoryTest extends TestCase {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationVersionRepository versionRepository;

    @PersistenceContext
    private EntityManager em;

    private ApplicationEntity application = new ApplicationEntity();

    private ApplicationVersionEntity version1 = new ApplicationVersionEntity();

    private ApplicationVersionEntity version2 = new ApplicationVersionEntity();

    @Before
    public void setUp() throws Exception {
        // First version
        version1.setApplicationVersion("1");
        versionRepository.save(version1);
        // Second version
        version2.setApplicationVersion("3");
        versionRepository.save(version2);

        // Add all versions
        ArrayList<ApplicationVersionEntity> applicationVersionEntities = newArrayList(version1, version2);

        // Build Application
        application.setId(1l);
        application.setAppName("Application");
        application.setAppVersions(applicationVersionEntities);

        ApplicationEntity save = applicationRepository.save(application);
        em.flush();
        em.clear();
    }

    @Test
    public void testManyToMany() {
        List<ApplicationEntity> applications = applicationRepository.findAll();
        assertThat(applications).isNotNull();
        assertThat(applications).isNotEmpty();
    }

    @Test
    public void testAppHasMultipleVersions() throws Exception {
        ApplicationEntity applications = applicationRepository.findOne(1l);
        Collection<ApplicationVersionEntity> appVersions = applications.getAppVersions();
        assertThat(appVersions.size()).isEqualTo(2);
    }

    @Test
    public void testVersionsHaveSameApp() throws Exception {
        List<ApplicationVersionEntity> versions = versionRepository.findAll();
        List<ApplicationVersionEntity> apps1 = versions.get(0).getApplications();
        List<ApplicationVersionEntity> apps2 = versions.get(1).getApplications();
        assertThat(apps1.get(0)).isEqualTo(apps2.get(0));
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories("org.zalando.stups.fullstop.violation.repository")
    @EntityScan("org.zalando.stups.fullstop.violation")
    @EnableJpaAuditing
    static class TestConfig {

        @Bean
        DataSource dataSource() throws IOException {
            return embeddedPostgres().getPostgresDatabase();
        }

        @Bean
        EmbeddedPostgreSQL embeddedPostgres() throws IOException {
            return EmbeddedPostgreSQL.start();
        }

        @Bean
        AuditorAware<String> auditorAware() {
            return () -> "unit-test";
        }
    }
}