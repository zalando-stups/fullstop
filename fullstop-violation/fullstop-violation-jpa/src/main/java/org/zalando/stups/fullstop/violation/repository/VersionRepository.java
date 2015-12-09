package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

/**
 * Created by gkneitschel.
 */
@Repository
public interface VersionRepository extends JpaRepository<VersionEntity, Long> {
    VersionEntity findByName(String name);
}
