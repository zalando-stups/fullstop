package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

/**
 * Created by gkneitschel.
 */
@Repository
public interface LifecycleRepository extends JpaRepository<LifecycleEntity, Long> {

    LifecycleEntity findByInstanceIdAndApplicationEntityAndVersionEntityAndRegion(String instanceId,
            ApplicationEntity applicationEntity, VersionEntity versionEntity, String region);
}
