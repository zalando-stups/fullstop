package org.zalando.stups.fullstop.plugin.scm;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

@Component
public class ScmRepositoryPlugin extends AbstractEC2InstancePlugin {

    private static final String URL = "url";

    private final Logger log = getLogger(getClass());

    private final KontrollettiOperations kontrollettiOperations;

    private final ViolationSink violationSink;

    @Autowired
    public ScmRepositoryPlugin(
            final EC2InstanceContextProvider contextProvider,
            final KontrollettiOperations kontrollettiOperations,
            final ViolationSink violationSink) {
        super(contextProvider);
        this.kontrollettiOperations = kontrollettiOperations;
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES).or(isEqual(START_INSTANCES));
    }

    @Override
    protected void process(final EC2InstanceContext context) {
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
                                    "application_id", app.getId(),
                                    "deployment_artifact", context.getSource().orElse(""),
                                    "scm_source", new Yaml().dump(scmSource)))
                            .build());
            return;
        }

        final String normalizedKioScmUrl = kontrollettiOperations.normalizeRepositoryUrl(kioScmUrl);
        final String normalizedScmSourceUrl = kontrollettiOperations.normalizeRepositoryUrl(scmSourceUrl);

        if (!Objects.equals(normalizedKioScmUrl, normalizedScmSourceUrl)) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(SCM_URL_NOT_MATCH_WITH_KIO)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", app.getId(),
                                    "normalized_scm_source_url", normalizedScmSourceUrl,
                                    "normalized_kio_scm_url", normalizedKioScmUrl)).build());
            return;
        }

        if (kontrollettiOperations.getRepository(normalizedScmSourceUrl) == null) {
            violationSink.put(
                    context.violation()
                            .withPluginFullyQualifiedClassName(ScmRepositoryPlugin.class)
                            .withType(ILLEGAL_SCM_REPOSITORY)
                            .withMetaInfo(ImmutableMap.of(
                                    "application_id", app.getId(),
                                    "normalized_scm_url", normalizedScmSourceUrl))
                            .build()
            );
        }
    }
}
