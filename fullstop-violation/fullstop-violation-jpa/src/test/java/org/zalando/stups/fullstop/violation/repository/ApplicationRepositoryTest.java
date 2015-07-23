/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ViolationRepositoryTest.TestConfig.class)
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