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
