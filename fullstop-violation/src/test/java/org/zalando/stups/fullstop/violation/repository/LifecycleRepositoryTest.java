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
import org.joda.time.DateTime;
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
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gkneitschel.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LifecycleRepositoryTest.TestConfig.class)
@Transactional
public class LifecycleRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    @PersistenceContext
    private EntityManager em;

    private ApplicationEntity application1;

    private ApplicationEntity application2;

    private VersionEntity version1;

    private VersionEntity version2;

    private LifecycleEntity lifecycleEntity1;

    private LifecycleEntity lifecycleEntity2;

    private ApplicationEntity savedApplication1;

    private LifecycleEntity savedLifecycleEntity1;

    private LifecycleEntity savedLifecycleEntity2;

    private VersionEntity savedVersion1;

    @Before
    public void setUp() throws Exception {
        // First version
        version1 = new VersionEntity();
        version1.setName("0.0.1");
        savedVersion1 = versionRepository.save(version1);
        // Second version
        version2 = new VersionEntity();
        version2.setName("0.9.3-SNAPSHOT");
        versionRepository.save(version2);

        // Add all versions
        List<VersionEntity> versionEntities = newArrayList(version1, version2);

        // Build Application1
        application1 = new ApplicationEntity();
        application1.setName("Application");
        application1.setVersionEntities(versionEntities);

        savedApplication1 = applicationRepository.save(application1);

        //Build first lifecycle
        lifecycleEntity1 = new LifecycleEntity();
        lifecycleEntity1.setRegion("eu-west-1");
        lifecycleEntity1.setEventDate(new DateTime(2015, 6, 23, 8, 14));
        lifecycleEntity1.setVersionEntity(version1);
        lifecycleEntity1.setApplicationEntity(application1);
        savedLifecycleEntity1 = lifecycleRepository.save(lifecycleEntity1);

        // Build second lifecycle
        lifecycleEntity2 = new LifecycleEntity();
        lifecycleEntity2.setRegion("eu-east-1");
        lifecycleEntity2.setVersionEntity(version1);
        lifecycleEntity2.setApplicationEntity(application1);
        savedLifecycleEntity2 = lifecycleRepository.save(lifecycleEntity2);

        em.flush();
        em.clear();
    }

    @Test
    public void testLifecycleHasVersion() throws Exception {
        LifecycleEntity lce = lifecycleRepository.findOne(savedLifecycleEntity1.getId());
        assertThat(lce.getVersionEntity().getName()).isEqualTo(savedVersion1.getName());
    }

    @Test
    public void testLifecycleHasApplication() throws Exception {
        LifecycleEntity lce = lifecycleRepository.findOne(savedLifecycleEntity1.getId());
        assertThat(lce.getApplicationEntity().getId()).isEqualTo(savedApplication1.getId());
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