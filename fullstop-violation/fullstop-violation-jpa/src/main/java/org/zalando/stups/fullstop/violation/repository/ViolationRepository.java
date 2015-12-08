package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

/**
 * Created by gkneitschel.
 */
@Repository
public interface ViolationRepository extends JpaRepository<ViolationEntity, Long>, ViolationRepositoryCustom {

}
