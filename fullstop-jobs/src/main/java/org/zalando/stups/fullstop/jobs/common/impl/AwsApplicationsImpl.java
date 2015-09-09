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
package org.zalando.stups.fullstop.jobs.common.impl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AwsApplicationsImpl implements AwsApplications {

    private final Logger log = getLogger(getClass());

    private final ApplicationLifecycleService applicationLifecycleService;
    private final KioOperations kioOperations;

    @Autowired
    public AwsApplicationsImpl(final ApplicationLifecycleService applicationLifecycleService,
                               final KioOperations kioOperations) {
        this.applicationLifecycleService = applicationLifecycleService;
        this.kioOperations = kioOperations;
    }

    @Override
    public Optional<Boolean> isPubliclyAccessible(final String accountId, final String region,
                                                  final List<String> instanceIds) {
        try {
            return Optional.ofNullable(applicationLifecycleService.findAppByInstanceIds(accountId, region, instanceIds))
                    .map(ApplicationEntity::getName)
                    .map(kioOperations::getApplicationById)
                    .map(Application::isPubliclyAccessible);
        } catch (final NotFoundException e) {
            log.warn(e.toString());
            return Optional.empty();
        }
    }
}
