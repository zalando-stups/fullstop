package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;

/**
 * Created by gkneitschel.
 */
@Repository
public interface LifecycleRepository extends JpaRepository<LifecycleEntity, Long>, LifecycleRepositoryCustom {
}
