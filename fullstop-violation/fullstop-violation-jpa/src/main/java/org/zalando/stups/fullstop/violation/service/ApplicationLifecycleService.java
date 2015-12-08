package org.zalando.stups.fullstop.violation.service;

import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;

import java.util.Collection;

/**
 * Created by gkneitschel.
 */
public interface ApplicationLifecycleService {

    LifecycleEntity saveLifecycle(ApplicationEntity applicationEntity, VersionEntity versionEntity,
            LifecycleEntity lifecycleEntity);

    LifecycleEntity saveInstanceLogLifecycle(String instanceId, DateTime instanceBootTime, String userdataPath,
            String region,
            String logData,
            String accountId);

    ApplicationEntity findAppByInstanceIds(String accountId, String region, Collection<String> instanceIds);
}
