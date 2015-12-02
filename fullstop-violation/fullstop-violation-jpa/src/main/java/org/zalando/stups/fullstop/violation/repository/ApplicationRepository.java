package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;

/**
 * Created by gkneitschel.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long>, ApplicationRepositoryCustom {
    ApplicationEntity findByName(String name);
}
