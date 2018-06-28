package org.zalando.stups.fullstop.violation.repository;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.stups.fullstop.violation.JpaConfig;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Created by gkneitschel.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JpaConfig.class)
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
        final List<VersionEntity> versionEntities = newArrayList(version1, version2);

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
        final LifecycleEntity lce = lifecycleRepository.findOne(savedLifecycleEntity1.getId());
        assertThat(lce.getVersionEntity().getName()).isEqualTo(savedVersion1.getName());
    }

    @Test
    public void testLifecycleHasApplication() throws Exception {
        final LifecycleEntity lce = lifecycleRepository.findOne(savedLifecycleEntity1.getId());
        assertThat(lce.getApplicationEntity().getId()).isEqualTo(savedApplication1.getId());
    }

    @Test
    public void testInstanceBootTime() throws Exception {
        final DateTime now = DateTime.now();

        final LifecycleEntity lifecycleEntity12 = new LifecycleEntity();
        lifecycleEntity12.setInstanceBootTime(now);
        lifecycleEntity12.setInstanceId("i-12345");
        lifecycleEntity12.setApplicationEntity(application1);
        lifecycleEntity12.setVersionEntity(version1);

        final LifecycleEntity saveLifecycleEntity = lifecycleRepository.save(lifecycleEntity12);

        assertThat(saveLifecycleEntity.getInstanceBootTime()).isEqualTo(now);
        assertThat(lifecycleRepository.findAll()).hasSize(3);

    }

    @Test
    public void TestFindByAppName() throws Exception{
        final ApplicationEntity app1 = new ApplicationEntity("App1");
        final ApplicationEntity app2 = new ApplicationEntity("App2");

        final VersionEntity vers1 = new VersionEntity("1.0");
        versionRepository.save(vers1);
        final VersionEntity vers2 = new VersionEntity("2.0");
        versionRepository.save(vers2);

        final List<VersionEntity> versionEntities = newArrayList(vers1,vers2);
        app1.setVersionEntities(versionEntities);
        applicationRepository.save(app1);
        app2.setVersionEntities(versionEntities);
        applicationRepository.save(app2);

        final LifecycleEntity lifecycleEntity1 = new LifecycleEntity();
        lifecycleEntity1.setApplicationEntity(app1);
        lifecycleEntity1.setVersionEntity(vers1);
        lifecycleRepository.save(lifecycleEntity1);

        final LifecycleEntity lifecycleEntity2 = new LifecycleEntity();
        lifecycleEntity2.setApplicationEntity(app1);
        lifecycleEntity2.setVersionEntity(vers2);
        lifecycleRepository.save(lifecycleEntity2);

        final LifecycleEntity lifecycleEntity3 = new LifecycleEntity();
        lifecycleEntity3.setApplicationEntity(app2);
        lifecycleEntity3.setVersionEntity(vers1);
        lifecycleRepository.save(lifecycleEntity3);

        final LifecycleEntity lifecycleEntity4 = new LifecycleEntity();
        lifecycleEntity4.setApplicationEntity(app2);
        lifecycleEntity4.setVersionEntity(vers2);
        lifecycleRepository.save(lifecycleEntity4);

        final LifecycleEntity lifecycleEntity5 = new LifecycleEntity();
        lifecycleEntity5.setApplicationEntity(app1);
        lifecycleEntity5.setVersionEntity(vers2);
        lifecycleRepository.save(lifecycleEntity5);

        final Page<LifecycleEntity> applications = lifecycleRepository.findByApplicationNameAndVersion("App1", null, new PageRequest(0, 4, ASC, "id"));
        assertThat(applications).hasSize(3);
        assertThat(applications.getTotalPages()).isEqualTo(1);
        final List<LifecycleEntity> content = applications.getContent();
        assertThat(content.get(1).getVersionEntity().getName()).isEqualTo(content.get(2).getVersionEntity().getName());
    }

    @Test
    public void TestfindByApplicationNameAndVersion() throws Exception{
        final ApplicationEntity app1 = new ApplicationEntity("App1");

        final VersionEntity vers1 = new VersionEntity("1.0");
        versionRepository.save(vers1);
        final VersionEntity vers2 = new VersionEntity("2.0");
        versionRepository.save(vers2);

        final List<VersionEntity> versionEntities = newArrayList(vers1,vers2);
        app1.setVersionEntities(versionEntities);
        applicationRepository.save(app1);

        final LifecycleEntity lifecycleEntity1 = new LifecycleEntity();
        lifecycleEntity1.setApplicationEntity(app1);
        lifecycleEntity1.setVersionEntity(vers1);
        lifecycleRepository.save(lifecycleEntity1);

        final LifecycleEntity lifecycleEntity2 = new LifecycleEntity();
        lifecycleEntity2.setApplicationEntity(app1);
        lifecycleEntity2.setVersionEntity(vers2);
        lifecycleRepository.save(lifecycleEntity2);

        final Page<LifecycleEntity> applications = lifecycleRepository.findByApplicationNameAndVersion("App1", "1.0", new PageRequest(0, 2, ASC, "id"));

        assertThat(applications).hasSize(1);
        assertThat(applications.getTotalPages()).isEqualTo(1);
        final List<LifecycleEntity> content = applications.getContent();

        assertThat(content.get(0).getVersionEntity().getName()).isEqualTo("1.0");
    }
}
