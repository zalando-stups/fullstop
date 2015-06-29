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

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import static com.google.common.collect.Lists.*;

/**
 * Created by gkneitschel.
 */
public class ApplicationLifecycleServiceImpl implements ApplicationLifecycleService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private LifecycleRepository lifecycleRepository;

    @Override
    public void save(ApplicationEntity applicationEntity, VersionEntity versionEntity,
            LifecycleEntity lifecycleEntity) {
        versionRepository.save(versionEntity);

        applicationEntity.setVersionEntities(newArrayList(versionEntity));
        applicationRepository.save(applicationEntity);

        lifecycleEntity.setApplicationEntity(applicationEntity);
        lifecycleRepository.save(lifecycleEntity);
    }
}
