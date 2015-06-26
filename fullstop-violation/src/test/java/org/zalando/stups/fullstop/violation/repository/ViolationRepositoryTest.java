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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Direction.ASC;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ViolationRepositoryTest.TestConfig.class)
@Transactional
public class ViolationRepositoryTest {

    @Autowired
    private ViolationRepository violationRepository;

    @PersistenceContext
    private EntityManager em;

    private ViolationEntity vio1;

    private ViolationEntity vio2;

    private ViolationEntity vio3;

    private ViolationEntity vio4;

    @Before
    public void setUp() throws Exception {
        vio1 = violationRepository
                .save(new ViolationEntity("runThatThing", "acc1", "germany-east-1", "foobar", null, "a comment"));
        vio2 = violationRepository
                .save(new ViolationEntity("runThatThing", "acc1", "germany-east-1", "blabla", null, null));
        vio3 = violationRepository
                .save(new ViolationEntity("runThatThing", "acc2", "germany-east-1", "lol", null, "no comment ;-)"));
        vio4 = violationRepository
                .save(new ViolationEntity("runThatThing", "acc3", "germany-east-1", "rofl", null, null));

        em.flush();
        em.clear();
    }

    @Test
    public void testGetAllPage1() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("id", Long.class)
                                       .isEqualTo(newArrayList(vio1.getId(), vio2.getId(), vio3.getId()));
    }

    @Test
    public void testGetAllPage2() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, null, new PageRequest(1, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting("id", Long.class).isEqualTo(newArrayList(vio4.getId()));
    }

    @Test
    public void testGetByAccounts() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(newArrayList("acc2", "acc3"), null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("id", Long.class)
                                       .isEqualTo(newArrayList(vio3.getId(), vio4.getId()));

    }

    @Test
    public void testGetViolationsSince1() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, vio4.getCreated().plusSeconds(1), null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void testGetViolationsSince2() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, vio2.getCreated(), null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("id", Long.class)
                                       .isEqualTo(newArrayList(vio3.getId(), vio4.getId()));
    }

    @Test
    public void testGetViolationsBeginningFrom() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, vio3.getId(), null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("id", Long.class)
                                       .isEqualTo(newArrayList(vio3.getId(), vio4.getId()));
    }

    @Test
    public void testGetCheckedViolations() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, true, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("id", Long.class)
                                       .isEqualTo(newArrayList(vio1.getId(), vio3.getId()));
    }

    @Test
    public void testGetUncheckedViolations() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, false, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("id", Long.class)
                                       .isEqualTo(newArrayList(vio2.getId(), vio4.getId()));
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories("org.zalando.stups.fullstop.violation.repository")
    @EntityScan("org.zalando.stups.fullstop.violation")
    @EnableJpaAuditing
    static class TestConfig {

        @Bean DataSource dataSource() throws IOException {
            return embeddedPostgres().getPostgresDatabase();
        }

        @Bean EmbeddedPostgreSQL embeddedPostgres() throws IOException {
            return EmbeddedPostgreSQL.start();
        }

        @Bean AuditorAware<String> auditorAware() {
            return () -> "unit-test";
        }
    }
}