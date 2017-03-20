package org.zalando.stups.fullstop.plugin.scm;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.scm.config.ScmRepositoryPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.ILLEGAL_SCM_REPOSITORY;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_IS_MISSING_IN_KIO;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_NOT_MATCH_WITH_KIO;

public class ScmRepositoryPlugin extends AbstractEC2InstancePlugin {

    private static final String URL = "url";

    private final Logger log = getLogger(getClass());

    private final Repositories repositories;
    private final ViolationSink violationSink;
    private final ScmRepositoryPluginProperties properties;

    public ScmRepositoryPlugin(
            final EC2InstanceContextProvider contextProvider,
            final Repositories repositories,
            final ViolationSink violationSink,
            final ScmRepositoryPluginProperties properties) {
        super(contextProvider);
        this.repositories = repositories;
        this.violationSink = violationSink;
        this.properties = properties;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES).or(isEqual(START_INSTANCES));
    }

    @Override
    protected void process(final EC2InstanceContext context) {
        final Optional<Map<String, String>> optionalScmSource = context.getScmSource();
        if (!optionalScmSource.isPresent()) {
            log.warn("Missing Scm source for {}. Skip ScmRepositoryPlugin.", context);
            return;
        }

        final Map<String, String> scmSource = optionalScmSource.get();
        final String scmSourceUrl = scmSource.get(URL);
        if (isBlank(scmSourceUrl)) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", context.getApplicationId().orElse(""),
                                    "deployment_artifact", context.getSource().orElse(""),
                                    "scm_source", new Yaml().dump(scmSource)))
                            .build());
            return;
        }

        final Repository scmSourceRepository;
        try {
            scmSourceRepository = repositories.parse(scmSourceUrl);
        } catch (final UnknownScmUrlException e) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(ILLEGAL_SCM_REPOSITORY)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", context.getApplicationId().orElse(""),
                                    "error_message", e.getMessage()))
                            .build()
            );

            return;
        }

        final String allowedOwnerPattern = Optional.ofNullable(properties.getHosts())
                .map(hostsByProvider -> hostsByProvider.get(scmSourceRepository.getProvider()))
                .map(ownersByHost -> ownersByHost.get(scmSourceRepository.getHost()))
                .orElseThrow(() -> new IllegalStateException(
                        format("Could not find allowedOwnerPattern for %s. Available properties: %s",
                                scmSourceRepository,
                                properties.getHosts())));
        if (!scmSourceRepository.getOwner().matches(allowedOwnerPattern)) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(ILLEGAL_SCM_REPOSITORY)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", context.getApplicationId().orElse(""),
                                    "normalized_scm_source_url", scmSourceRepository.toString(),
                                    "allowed_owners", allowedOwnerPattern,
                                    "actual_owner", scmSourceRepository.getOwner()))
                            .build()
            );
        }

        final Optional<Application> optionalKioApp = context.getKioApplication();
        if (!optionalKioApp.isPresent()) {
            log.warn("Missing Kio App for {}. Skip ScmRepositoryPlugin.", context);
            return;
        }

        final Application app = optionalKioApp.get();
        final String kioScmUrl = app.getScmUrl();
        if (isBlank(kioScmUrl)) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(SCM_URL_IS_MISSING_IN_KIO)
                            .withMetaInfo(singletonMap("application_id", app.getId()))
                            .build());
            return;
        }

        try {
            final Repository kioRepository = repositories.parse(kioScmUrl);
            if (!scmSourceRepository.equals(kioRepository)) {
                violationSink.put(
                        context.violation()
                                .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                                .withType(SCM_URL_NOT_MATCH_WITH_KIO)
                                .withMetaInfo(ImmutableMap.of(
                                        "application_id", app.getId(),
                                        "normalized_scm_source_url", scmSourceRepository.toString(),
                                        "normalized_kio_scm_url", kioRepository.toString())).build());
            }
        } catch (UnknownScmUrlException e) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(SCM_URL_NOT_MATCH_WITH_KIO)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", app.getId(),
                                    "normalized_scm_source_url", scmSourceRepository.toString(),
                                    "kio_scm_url", kioScmUrl,
                                    "error_message", e.getMessage())).build());
        }
    }
}
