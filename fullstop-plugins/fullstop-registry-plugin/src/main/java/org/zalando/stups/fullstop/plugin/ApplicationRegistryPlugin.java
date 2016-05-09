package org.zalando.stups.fullstop.plugin;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

public class ApplicationRegistryPlugin extends AbstractEC2InstancePlugin {

    private final Logger log = getLogger(getClass());

    private final RegistryPluginProperties registryPluginProperties;

    private final ViolationSink violationSink;

    public ApplicationRegistryPlugin(final EC2InstanceContextProvider contextProvider,
                                     final ViolationSink violationSink,
                                     final RegistryPluginProperties registryPluginProperties) {
        super(contextProvider);
        this.violationSink = violationSink;
        this.registryPluginProperties = registryPluginProperties;
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
            return;
        }

        final List<Approval> approvals = context.getKioApprovals();
        final List<String> mandatoryApprovals = registryPluginProperties.getMandatoryApprovals();

        // #139
        // https://github.com/zalando-stups/fullstop/issues/139
        // does not have all default approval types
        final Set<String> approvalTypes = approvals.stream().map(Approval::getApprovalType).collect(toSet());
        if (!approvalTypes.containsAll(mandatoryApprovals))

        {
            final Set<String> diff = newHashSet(mandatoryApprovals);
            diff.removeAll(approvalTypes);
            violationSink.put(
                    context.violation()
                            .withType(MISSING_VERSION_APPROVAL)
                            .withPluginFullyQualifiedClassName(ApplicationRegistryPlugin.class)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", appId,
                                    "version_id", versionId,
                                    "missing_approval_types", diff))
                            .build());
        }

        // #140
        // https://github.com/zalando-stups/fullstop/issues/140
        // => code, test and deploy approvals have to be done by at least two different people
        // e.g. four-eyes-principle
        final int approverCount = approvals.stream()
                .filter(relevantApprovalTypes())
                .map(Approval::getUserId)
                .collect(toSet())
                .size();
        final int minApprovals = kioApplication.get().getRequiredApprovers();
        if (approverCount < minApprovals)

        {
            violationSink.put(
                    context.violation()
                            .withType(MISSING_VERSION_APPROVAL)
                            .withPluginFullyQualifiedClassName(ApplicationRegistryPlugin.class)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", appId,
                                    "version_id", versionId,
                                    "number_of_approvers", approverCount,
                                    "required_approvers", minApprovals))
                            .build());
        }
    }

    private Predicate<Approval> relevantApprovalTypes() {
        return a -> registryPluginProperties.getApprovalsFromMany().contains(a.getApprovalType());
    }
}
