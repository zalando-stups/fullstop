package org.zalando.stups.fullstop.violation.repository;

import org.zalando.stups.fullstop.violation.entity.AccountRegion;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;

import java.util.Collection;
import java.util.Set;

public interface ApplicationRepositoryCustom {

    ApplicationEntity findByInstanceIds(String accountId, String region, Collection<String> instanceIds);

    Set<AccountRegion> findDeployments(String applicationId);
}
