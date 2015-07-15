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
package org.zalando.stups.fullstop.plugin.scm;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Component
public class ScmRepositoryPlugin extends AbstractFullstopPlugin {

    private static final String EVENT_NAME = "RunInstances";

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String SOURCE = "source";

    private static final String APPLICATION_ID = "application_id";

    private static final Pattern DOCKER_SOURCE_PATTERN = Pattern.compile("^(?>.*/)?(.+):(.+)$");

    private static final String URL = "url";

    private final Logger log = getLogger(getClass());

    private final KioOperations kioOperations;

    private final PieroneOperations pieroneOperations;

    private final KontrollettiOperations kontrollettiOperations;

    private final UserDataProvider userDataProvider;

    private final ViolationSink violationSink;

    @Autowired
    public ScmRepositoryPlugin(
            final ViolationSink violationSink,
            final KioOperations kioOperations,
            final PieroneOperations pieroneOperations,
            final KontrollettiOperations kontrollettiOperations,
            final UserDataProvider userDataProvider) {
        this.kioOperations = kioOperations;
        this.pieroneOperations = pieroneOperations;
        this.kontrollettiOperations = kontrollettiOperations;
        this.userDataProvider = userDataProvider;
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        final CloudTrailEventData cloudTrailEventData = event.getEventData();
        final String eventSource = cloudTrailEventData.getEventSource();
        final String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        for (final String instanceId : getInstanceIds(event)) {
            final Map userData = userDataProvider.getUserData(event, instanceId);

            if (userData == null) {
                log.warn(
                        "No userData available for EC2 instance '{}'. Skip execution of ScmRepositoryPlugin!",
                        instanceId);
                return;
            }

            final String source = (String) userData.get(SOURCE);
            if (isBlank(source)) {
                log.warn(
                        "'Source' field is missing in userData of EC2 instance '{}'. Skip execution of ScmRepositoryPlugin!",
                        instanceId);
                return;
            }

            final Matcher sourceMatcher = DOCKER_SOURCE_PATTERN.matcher(source);
            if (!sourceMatcher.matches()) {
                log.warn(
                        "'{}' is not a valid docker source (or my regex is bulls**t). Skip execution of ScmRepositoryPlugin!",
                        source);
                return;
            }

            final String artifact = sourceMatcher.group(1);
            final String tag = sourceMatcher.group(2);

            final String applicationId = (String) userData.get(APPLICATION_ID);
            if (isBlank(applicationId)) {
                log.warn(
                        "'application_id' is missing in userData of EC2 instance {}. Skip execution of ScmRepositoryPlugin!",
                        instanceId);
                return;
            }

            final Application app = kioOperations.getApplicationById(applicationId);
            if (app == null) {
                log.warn("app '{}' does not exist in Kio. Skip execution of ScmRepositoryPlugin!", applicationId);
                return;
            }

            final String team = app.getTeamId();
            final String kioScmUrl = app.getScmUrl();
            if (isBlank(kioScmUrl)) {
                violationSink.put(violationFor(event).withMessage("Scm URL is missing in Kio.").build());
                return;
            }

            final Map<String, String> scmSource = pieroneOperations.getScmSource(team, artifact, tag);
            if (scmSource == null) {
                log.warn("scm-source.json is missing. Skip execution of ScmRepositoryPlugin!");
                return;
            }

            final String scmSourceUrl = scmSource.get(URL);
            if (isBlank(scmSourceUrl)) {
                violationSink.put(violationFor(event).withMessage("scm-source.json does not contain the url").build());
                return;
            }

            final String normalizedKioScmUrl = kontrollettiOperations.normalizeRepositoryUrl(kioScmUrl);
            final String normalizedScmSourceUrl = kontrollettiOperations.normalizeRepositoryUrl(scmSourceUrl);

            if (!Objects.equals(normalizedKioScmUrl, normalizedScmSourceUrl)) {
                violationSink.put(
                        violationFor(event)
                                .withMessage("Scm url in scm-source.json does not match the url given in Kio")
                                .withViolationObject(
                                        ImmutableMap.of(
                                                "normalized_scm_source_url", normalizedScmSourceUrl,
                                                "normalized_kio_scm_url", normalizedKioScmUrl))
                                .build());
            }
        }
    }
}
