package org.zalando.stups.fullstop.rule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;

/**
 * @author Christian Lohmann
 */
@Repository
public interface RuleEntityRepository extends JpaRepository<RuleEntity, String> {

    RuleEntity findOne(Long id);
}
