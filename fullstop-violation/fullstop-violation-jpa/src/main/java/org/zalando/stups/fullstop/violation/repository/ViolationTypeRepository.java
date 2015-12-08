package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

/**
 * Created by mrandi.
 */
@Repository
public interface ViolationTypeRepository extends JpaRepository<ViolationTypeEntity, String> {

}
