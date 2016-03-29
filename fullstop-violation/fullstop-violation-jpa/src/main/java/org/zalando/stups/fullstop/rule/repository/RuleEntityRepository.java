package org.zalando.stups.fullstop.rule.repository;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;

import java.util.List;

/**
 * @author Christian Lohmann
 */
@Repository
public interface RuleEntityRepository extends JpaRepository<RuleEntity, Long> {
    List<RuleEntity> findByExpiryDateAfter(DateTime dateTime);
}
