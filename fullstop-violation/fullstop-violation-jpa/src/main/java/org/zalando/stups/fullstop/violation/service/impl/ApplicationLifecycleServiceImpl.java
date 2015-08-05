/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import javax.transaction.Transactional;
import java.util.Base64;
import java.util.Map;

/**
 * Created by gkneitschel.
 */
@Service
public class ApplicationLifecycleServiceImpl implements ApplicationLifecycleService {

    private final Logger log = LoggerFactory.getLogger(ApplicationLifecycleServiceImpl.class);


    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    @Override
    @Transactional
    public LifecycleEntity saveLifecycle(final ApplicationEntity applicationEntity, final VersionEntity versionEntity,
            final LifecycleEntity lifecycleEntity) {

        if (applicationEntity == null || versionEntity == null || lifecycleEntity == null) {
            throw new RuntimeException("saveLifecycle: One or more parameters are null!");
        }

        ApplicationEntity applicationByName = applicationRepository.findByName(applicationEntity.getName());
        VersionEntity versionByName = versionRepository.findByName(versionEntity.getName());
        LifecycleEntity lifecycleByInstanceId =
                lifecycleRepository.findByInstanceIdAndApplicationEntityAndVersionEntityAndRegion(
                        lifecycleEntity.getInstanceId(), applicationByName, versionByName, lifecycleEntity.getRegion());

        if (applicationByName == null) {
            applicationByName = applicationRepository.save(applicationEntity);
        }

        if (versionByName == null) {
            versionByName = versionRepository.save(versionEntity);
        }

        if (lifecycleByInstanceId != null) {
            lifecycleByInstanceId.setEventDate(lifecycleEntity.getEventDate());
            lifecycleByInstanceId.setEventType(lifecycleEntity.getEventType());
            lifecycleByInstanceId = lifecycleRepository.save(lifecycleByInstanceId);
            return lifecycleByInstanceId;
        }

        if (!applicationByName.getVersionEntities().contains(versionByName)) {
            applicationByName.getVersionEntities().add(versionByName);
            applicationRepository.save(applicationByName);
        }

        lifecycleEntity.setApplicationEntity(applicationByName);
        lifecycleEntity.setVersionEntity(versionByName);

        LifecycleEntity savedLifecycleEntity = lifecycleRepository.save(lifecycleEntity);
        return savedLifecycleEntity;
    }

    @Override
    public LifecycleEntity saveInstanceLogLifecycle(final String instanceId, final DateTime instanceBootTime,
            final String userdataPath, final String region, final String logData) {
        if (logData == null) {
            log.warn("Logdata mus not be empty!");
            return new LifecycleEntity();
        }
        Yaml yaml = new Yaml();
        String decodedLogData = new String(Base64.getDecoder().decode(logData));

        Map userdata = (Map) yaml.load(decodedLogData);

        ApplicationEntity applicationEntity = new ApplicationEntity(userdata.get("application_id").toString());

        VersionEntity versionEntity = new VersionEntity(userdata.get("application_version").toString());

        LifecycleEntity lifecycleEntity = new LifecycleEntity();
        lifecycleEntity.setInstanceBootTime(instanceBootTime);
        lifecycleEntity.setInstanceId(instanceId);
        lifecycleEntity.setRegion(region);
        lifecycleEntity.setUserdataPath(userdataPath);

        LifecycleEntity savedLifecycleEntity = saveLifecycle(applicationEntity, versionEntity, lifecycleEntity);

        return savedLifecycleEntity;
    }
}
