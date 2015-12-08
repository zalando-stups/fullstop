package org.zalando.stups.fullstop.violation.repository;

import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;

import java.util.Collection;

public interface ApplicationRepositoryCustom {

    ApplicationEntity findByInstanceIds(String accountId, String region, Collection<String> instanceIds);
}
