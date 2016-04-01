package org.zalando.stups.fullstop.violation.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.Yaml;
import org.zalando.stups.fullstop.violation.entity.AccountRegion;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import javax.annotation.Resource;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import java.util.*;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

/**
 * Created by gkneitschel.
 */
@Service(ApplicationLifecycleServiceImpl.BEAN_NAME)
public class ApplicationLifecycleServiceImpl implements ApplicationLifecycleService {

    protected static final String BEAN_NAME = "applicationLifecycleService";

    private final Base64.Decoder base64Decoder = Base64.getMimeDecoder();

    private final Logger log = LoggerFactory.getLogger(ApplicationLifecycleServiceImpl.class);


    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    @Resource(name = BEAN_NAME)
    private ApplicationLifecycleService self;

    @Override
    @Retryable(maxAttempts = 10, backoff = @Backoff(delay = 100, maxDelay = 500),
            include = {ObjectOptimisticLockingFailureException.class, OptimisticLockException.class, DataIntegrityViolationException.class})
    @Transactional(REQUIRES_NEW)
    public LifecycleEntity saveLifecycle(final ApplicationEntity applicationEntity, final VersionEntity versionEntity,
            final LifecycleEntity lifecycleToSave) {
        Assert.notNull(applicationEntity, "applicationEntity must not be null");
        Assert.notNull(versionEntity, "versionEntity must not be null");
        Assert.notNull(lifecycleToSave, "lifecycleToSave must not be null");

        ApplicationEntity applicationByName = applicationRepository.findByName(applicationEntity.getName());
        VersionEntity versionByName = versionRepository.findByName(versionEntity.getName());

        if (applicationByName == null) {
            applicationByName = applicationRepository.save(applicationEntity);
        }

        if (versionByName == null) {
            versionByName = versionRepository.save(versionEntity);
        }

        if (!applicationByName.getVersionEntities().contains(versionByName)) {
            applicationByName.getVersionEntities().add(versionByName);
            applicationByName = applicationRepository.save(applicationByName);
        }


        lifecycleToSave.setApplicationEntity(applicationByName);
        lifecycleToSave.setVersionEntity(versionByName);

        return lifecycleRepository.save(lifecycleToSave);
    }

    @Override
    public LifecycleEntity saveInstanceLogLifecycle(final String instanceId, final DateTime instanceBootTime,
            final String userdataPath, final String region, final String logData, final String accountId) {
        final Yaml yaml = new Yaml();
        final Optional<Map> taupageYaml = Optional.ofNullable(logData)
                .map(base64Decoder::decode)
                .map(String::new)
                .map(yaml::load)
                .filter(data -> data instanceof Map)
                .map(map -> (Map) map);

        final Optional<ApplicationEntity> application = taupageYaml
                .map(yamlMap -> yamlMap.get("application_id"))
                .map(String::valueOf)
                .map(ApplicationEntity::new);

        final Optional<VersionEntity> version = taupageYaml
                .map(yamlMap -> yamlMap.get("application_version"))
                .map(String::valueOf)
                .map(VersionEntity::new);

        if (application.isPresent() && version.isPresent()) {
            final LifecycleEntity lifecycleEntity = new LifecycleEntity();
            lifecycleEntity.setInstanceBootTime(instanceBootTime);
            lifecycleEntity.setInstanceId(instanceId);
            lifecycleEntity.setAccountId(accountId);
            lifecycleEntity.setRegion(region);
            lifecycleEntity.setUserdataPath(userdataPath);

            return self.saveLifecycle(application.get(), version.get(), lifecycleEntity);
        } else {
            log.warn("Empty or invalid taupage yaml.");
            return null;
        }

    }

    @Override
    public ApplicationEntity findAppByInstanceIds(String accountId, String region, Collection<String> instanceIds) {
        return applicationRepository.findByInstanceIds(accountId, region, instanceIds);
    }

    @Override
    public Set<AccountRegion> findDeployments(String applicationId) {
        return applicationRepository.findDeployments(applicationId);
    }
}
