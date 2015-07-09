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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import javax.transaction.Transactional;

/**
 * Created by gkneitschel.
 */
@Service
public class ApplicationLifecycleServiceImpl implements ApplicationLifecycleService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    @Override
    @Transactional
    public void saveLifecycle(ApplicationEntity applicationEntity, VersionEntity versionEntity,
            LifecycleEntity lifecycleEntity) {

        if (applicationEntity == null || versionEntity == null || lifecycleEntity == null) {
            throw new RuntimeException("saveLifecycle: One or more parameters are null!");
        }

        ApplicationEntity applicationByName = applicationRepository.findByName(applicationEntity.getName());
        VersionEntity versionByName = versionRepository.findByName(versionEntity.getName());

        if(applicationByName == null){
            applicationByName = applicationRepository.save(applicationEntity);
        }

        if (versionByName == null) {
           versionByName = versionRepository.save(versionEntity);
        }

        if(lifecycleEntity.getId() != null){
            throw new UnsupportedOperationException("No update possible for Lifecycle Entity");
        }

        // applicationEntity has not versionEntity
        if(!applicationByName.getVersionEntities().contains(versionByName)){
            applicationByName.getVersionEntities().add(versionByName);
            applicationRepository.save(applicationByName);
        }

        if(!versionByName.getApplicationEntities().contains(applicationByName)){

            versionByName.getApplicationEntities().add(applicationByName);
            versionRepository.save(versionByName);
        }

        lifecycleEntity.setApplicationEntity(applicationByName);
        lifecycleEntity.setVersionEntity(versionByName);
        lifecycleRepository.save(lifecycleEntity);
    }
}
