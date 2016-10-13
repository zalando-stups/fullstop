package org.zalando.stups.fullstop.plugin;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.ViolationType;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.SPEC_URL_IS_MISSING_IN_KIO;

@Component
public class ApplicationMasterdataPlugin extends AbstractEC2InstancePlugin {

    private final Logger log = getLogger(getClass());

    private final ViolationSink violationSink;

    @Autowired
    public ApplicationMasterdataPlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES).or(isEqual(START_INSTANCES));
    }

    @Override
    protected void process(final EC2InstanceContext context) {
        final Optional<String> optionalAppId = context.getApplicationId();
        if (!optionalAppId.isPresent()) {
            log.warn("no application id found for {}. Skip execution of ApplicationMasterdataPlugin", context);
            return;
        }

        final String applicationId = optionalAppId.get();
        final Optional<Application> optionalKioApp = context.getKioApplication();
        if (!optionalKioApp.isPresent()) {
            violationSink.put(context.violation()
                    .withType(ViolationType.APPLICATION_NOT_PRESENT_IN_KIO)
                    .withPluginFullyQualifiedClassName(ApplicationMasterdataPlugin.class)
                    .withMetaInfo(singletonMap("application_id", applicationId))
                    .build());
            return;
        }

        final Application kioApp = optionalKioApp.get();

        if (isBlank(kioApp.getSpecificationUrl())) {
            violationSink.put(context.violation()
                    .withType(SPEC_URL_IS_MISSING_IN_KIO)
                    .withPluginFullyQualifiedClassName(ApplicationMasterdataPlugin.class)
                    .withMetaInfo(singletonMap("application_id", applicationId))
                    .build());
        }
    }
}
