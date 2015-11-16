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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.zalando.stups.fullstop.violation.entity.CountByAccountAndType;
import org.zalando.stups.fullstop.violation.entity.CountByAppVersionAndType;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.springframework.data.domain.Sort.Direction.ASC;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ViolationRepositoryTest.TestConfig.class)
@Transactional
public class ViolationRepositoryTest {

    @Autowired
    private ViolationRepository violationRepository;
    @Autowired
    private ViolationTypeRepository violationTypeRepository;

    @PersistenceContext
    private EntityManager em;

    private ViolationEntity vio1;

    private ViolationEntity vio2;

    private ViolationEntity vio3;

    private ViolationEntity vio4;

    private ViolationEntity vio5;

    private Map<String, String> metaInfoMap = singletonMap("test", "jsonSerialization");
    private List<String> metaInfoList = newArrayList("test", "jsonSerialization");

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        final ViolationTypeEntity type1 = violationTypeRepository.saveAndFlush(new ViolationTypeEntity("SOMETHING_WENT_WRONG"));
        final ViolationTypeEntity type2 = violationTypeRepository.saveAndFlush(new ViolationTypeEntity("YOU_SCREWED_UP"));

        vio1 = save(new ViolationEntity("run01", "acc1", "germany-east-1", "i-1234", metaInfoMap, "a comment", "username"), type1);
        vio2 = save(new ViolationEntity("run02", "acc1", "germany-east-1", "i-5678", metaInfoList, null, "username"), type1);
        vio3 = save(new ViolationEntity("run03", "acc2", "germany-east-1", "i-1234", null, "no comment ;-)", "username"), type2);
        vio4 = save(new ViolationEntity("run04", "acc3", "germany-east-1", "i-1234", null, null, "username"), type1);
        vio5 = save(new ViolationEntity("run05", "acc3", "germany-east-1", "i-5678", null, null, "username"), type2);

        em.flush();
        em.clear();
    }

    private ViolationEntity save(ViolationEntity entity, ViolationTypeEntity type) {
        entity.setViolationTypeEntity(type);
        return violationRepository.save(entity);
    }


    @Test
    public void testGetAllPage1() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, null, null, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("id", Long.class)
                .isEqualTo(newArrayList(vio1.getId(), vio2.getId(), vio3.getId()));
        assertThat(result.getContent()).extracting("username", String.class)
                .isEqualTo(newArrayList(vio1.getUsername(), vio2.getUsername(), vio3.getUsername()));
    }

    @Test
    public void testGetAllPage2() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, null, null, null, null, null, new PageRequest(1, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("id", Long.class).isEqualTo(newArrayList(vio4.getId(), vio5.getId()));
    }

    @Test
    public void testGetByAccounts() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(newArrayList("acc2", "acc3"), null, null, null, null, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("id", Long.class)
                .isEqualTo(newArrayList(vio3.getId(), vio4.getId(), vio5.getId()));

    }

    @Test
    public void testGetViolationsSince1() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, vio4.getCreated().plusSeconds(1), null, null, null, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void testGetViolationsSince2() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, vio2.getCreated(), null, null, null, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("id", Long.class)
                .isEqualTo(newArrayList(vio3.getId(), vio4.getId(), vio5.getId()));
    }

    @Test
    public void testGetViolationsBeginningFrom() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, vio3.getId(), null, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("id", Long.class)
                .isEqualTo(newArrayList(vio3.getId(), vio4.getId(), vio5.getId()));
    }

    @Test
    public void testGetCheckedViolations() throws Exception {
        final Page<ViolationEntity> result = violationRepository
                .queryViolations(null, null, null, null, true, null, null, null, new PageRequest(0, 3, ASC, "id"));

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
                .queryViolations(null, null, null, null, false, null, null, null, new PageRequest(0, 3, ASC, "id"));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("id", Long.class)
                .isEqualTo(newArrayList(vio2.getId(), vio4.getId(), vio5.getId()));
    }

    @Test
    public void testCountViolationsByAccountAndType() throws Exception {
        final List<CountByAccountAndType> result = violationRepository.countByAccountAndType(emptySet(), empty(), empty(), empty());
        assertThat(result).hasSize(4);
    }

    @Test
    public void testCountViolationsByAppVersionAndType() throws Exception {
        final List<CountByAppVersionAndType> result = violationRepository.countByAppVersionAndType(
                "acc1",
                Optional.of(now().minusDays(1)),
                Optional.of(now().plusDays(1)),
                Optional.of(FALSE));
        assertThat(result).hasSize(1);
    }

    @Test
    public void testMetadataObjectJsonMap() throws Exception {
        ViolationEntity one = violationRepository.getOne(vio1.getId());
        assertThat(one.getMetaInfo()).isEqualTo(objectMapper.writeValueAsString(metaInfoMap));
    }

    @Test
    public void testMetadataObjectJsonList() throws Exception {
        ViolationEntity one = violationRepository.getOne(vio2.getId());
        assertThat(one.getMetaInfo()).isEqualTo(objectMapper.writeValueAsString(metaInfoList));
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
