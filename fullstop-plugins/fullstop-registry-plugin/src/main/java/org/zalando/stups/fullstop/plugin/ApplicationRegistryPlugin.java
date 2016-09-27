package org.zalando.stups.fullstop.plugin;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.APPLICATION_NOT_PRESENT_IN_KIO;
import static org.zalando.stups.fullstop.violation.ViolationType.APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT;
import static org.zalando.stups.fullstop.violation.ViolationType.APPLICATION_VERSION_NOT_PRESENT_IN_KIO;

public class ApplicationRegistryPlugin extends AbstractEC2InstancePlugin {

    private final Logger log = getLogger(getClass());

    private final ViolationSink violationSink;

    public ApplicationRegistryPlugin(final EC2InstanceContextProvider contextProvider,
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
            log.warn("Could not find application_id for {}. Will skip execution of ApplicationRegistryPlugin.", context);
            return;
        }

        final String appId = optionalAppId.get();

        final Optional<Application> kioApplication = context.getKioApplication();
        if (!kioApplication.isPresent()) {
            violationSink.put(
                    context.violation()
                            .withType(APPLICATION_NOT_PRESENT_IN_KIO)
                            .withPluginFullyQualifiedClassName(ApplicationRegistryPlugin.class)
                            .withMetaInfo(singletonMap("application_id", appId))
                            .build());
            return;
        }

        final Optional<String> optionalVersionId = context.getVersionId();
        if (!optionalVersionId.isPresent()) {
            log.warn("Could not find application_version for {}. Will skip execution of ApplicationRegistryPlugin.", context);
            return;
        }

        final String versionId = optionalVersionId.get();

        final Optional<Version> optionalKioVersion = context.getKioVersion();
        if (optionalKioVersion.isPresent()) {
            final Version kioVersion = optionalKioVersion.get();
            final String artifact = kioVersion.getArtifact();
            context.getSource().ifPresent(source -> {
                if (!artifact.contains(source)) {
                    violationSink.put(
                            context.violation()
                                    .withType(APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT)
                                    .withPluginFullyQualifiedClassName(ApplicationRegistryPlugin.class)
                                    .withMetaInfo(ImmutableMap.of(
                                            "application_id", appId,
                                            "application_version", versionId,
                                            "kio_artifact", artifact,
                                            "taupage_source", source))
                                    .build());
                }
            });
        } else {
            violationSink.put(
                    context.violation()
                            .withType(APPLICATION_VERSION_NOT_PRESENT_IN_KIO)
                            .withPluginFullyQualifiedClassName(ApplicationRegistryPlugin.class)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", appId,
                                    "application_version", versionId))
                            .build());
        }
    }
}
