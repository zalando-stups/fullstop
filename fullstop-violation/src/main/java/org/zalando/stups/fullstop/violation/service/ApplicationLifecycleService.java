package org.zalando.stups.fullstop.violation.service;

import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

/**
 * Created by gkneitschel.
 */
public interface ApplicationLifecycleService {
    void save(ApplicationEntity applicationEntity, VersionEntity versionEntity, LifecycleEntity lifecycleEntity);
}
