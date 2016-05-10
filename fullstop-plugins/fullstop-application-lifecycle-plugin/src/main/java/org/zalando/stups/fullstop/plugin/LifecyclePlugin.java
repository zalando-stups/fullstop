package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.ec2.model.Image;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getEventTime;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getRunInstanceTime;

@Component
public class LifecyclePlugin extends AbstractEC2InstancePlugin {

    private final Logger log = getLogger(getClass());

    private final ApplicationLifecycleService applicationLifecycleService;

    @Autowired
    public LifecyclePlugin(final EC2InstanceContextProvider contextProvider,
                           final ApplicationLifecycleService applicationLifecycleService) {
        super(contextProvider);
        this.applicationLifecycleService = applicationLifecycleService;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES)
                .or(isEqual(START_INSTANCES))
                .or(isEqual(STOP_INSTANCES))
                .or(isEqual(TERMINATE_INSTANCES));
    }

    @Override
    protected void process(final EC2InstanceContext context) {
        final LifecycleEntity lifecycleEntity = new LifecycleEntity();
        lifecycleEntity.setEventType(context.getEventName());
        lifecycleEntity.setEventDate(getLifecycleDate(context));
        lifecycleEntity.setAccountId(context.getAccountId());
        lifecycleEntity.setRegion(context.getRegionAsString());
        lifecycleEntity.setInstanceId(context.getInstanceId());
        context.getAmiId().ifPresent(lifecycleEntity::setImageId);
        context.getAmi().map(Image::getName).ifPresent(lifecycleEntity::setImageName);

        final Optional<ApplicationEntity> application = context.getApplicationId().map(ApplicationEntity::new);
        if (!application.isPresent()) {
            log.warn("Could not determine applicationId. Skip processing of LifecyclePlugin.");
            return;
        }

        final Optional<VersionEntity> version = context.getVersionId().map(VersionEntity::new);
        if (!version.isPresent()) {
            log.warn("Could not determine versionId. Skip processing of LifecyclePlugin.");
            return;
        }

        applicationLifecycleService.saveLifecycle(application.get(), version.get(), lifecycleEntity);
    }

    private DateTime getLifecycleDate(final EC2InstanceContext context) {
        if (context.getEventName().equals(RUN_INSTANCES)) {
            return getRunInstanceTime(context.getInstanceJson());
        } else {
            return getEventTime(context.getEvent());
        }
    }
}
