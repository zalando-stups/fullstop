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
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.zalando.stups.clients.kio.*;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

/**
 * @author mrandi
 */

@Component
public class RegistryPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    protected static String APPLICATION_ID = "application_id";

    protected static String APPLICATION_VERSION = "application_version";

    protected static String SOURCE = "source";

    private final RegistryPluginProperties registryPluginProperties;

    private final ViolationSink violationSink;

    private final PieroneOperations pieroneOperations;

    private final KioOperations kioOperations;

    private final UserDataProvider userDataProvider;

    @Autowired
    public RegistryPlugin(
            final UserDataProvider userDataProvider,
            final ViolationSink violationSink, final PieroneOperations pieroneOperations,
            final KioOperations kioOperations, final RegistryPluginProperties registryPluginProperties) {

        this.violationSink = violationSink;
        this.pieroneOperations = pieroneOperations;
        this.kioOperations = kioOperations;
        this.userDataProvider = userDataProvider;
        this.registryPluginProperties = registryPluginProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        List<String> instanceIds = getInstanceIds(event);

        for (String instanceId : instanceIds) {
            Map userData = getAndValidateUserData(
                    event,
                    instanceId);
            if (userData == null) {
                return;
            }

            String applicationId = getAndValidateApplicationId(
                    event,
                    userData,
                    instanceId);

            String applicationVersion = getAndValidateApplicationVersion(
                    event,
                    userData,
                    instanceId);

            String source = getAndValidateSource(
                    event,
                    userData,
                    instanceId);

            if (applicationId != null) {

                Application applicationFromKio = getAndValidateApplicationFromKio(
                        event,
                        applicationId);

                if (applicationFromKio != null) {

                    Version applicationVersionFromKio = getAndValidateApplicationVersionFromKio(
                            event,
                            applicationId,
                            applicationVersion);

                    if (applicationVersionFromKio != null) {

                        validateSourceWithPierone(
                                event,
                                applicationId,
                                applicationVersion,
                                applicationFromKio.getTeamId(),
                                source,
                                applicationVersionFromKio.getArtifact());

                        validateScmSource(
                                event,
                                applicationFromKio.getTeamId(),
                                applicationId,
                                applicationVersion);

                        validateContainsMandatoryApprovals(
                                applicationVersionFromKio,
                                event);
                        validateMultipleEyesPrinciple(
                                event,
                                applicationVersionFromKio.getApplicationId(),
                                applicationVersionFromKio.getId(),
                                applicationFromKio.getRequiredApprovers());
                    }
                }
            }
        }
    }

    protected Map getAndValidateUserData(CloudTrailEvent event, String instanceId) {
        Map userData;
        final String accountId = event.getEventData().getUserIdentity().getAccountId();
        final String region = event.getEventData().getAwsRegion();
        try {
            userData = userDataProvider.getUserData(
                    accountId, region,
                    instanceId);
        }
        catch (AmazonServiceException ex) {
            LOG.error(ex.getMessage());
            violationSink.put(
                    violationFor(event).withInstanceId(instanceId)
                                       .withType(MISSING_USER_DATA)
                                       .withPluginFullyQualifiedClassName(
                                               RegistryPlugin.class)
                                       .withMetaInfo(instanceId)
                                       .build());
            return null;
        }

        if (userData == null) {
            violationSink.put(
                    violationFor(event).withInstanceId(instanceId)
                                       .withType(MISSING_USER_DATA)
                                       .withPluginFullyQualifiedClassName(
                                               RegistryPlugin.class)
                                       .withMetaInfo(instanceId)
                                       .build());
            return null;
        }

        if (userData.isEmpty()) {
            violationSink.put(
                    violationFor(event).withInstanceId(instanceId)
                                       .withType(MISSING_USER_DATA)
                                       .withPluginFullyQualifiedClassName(
                                               RegistryPlugin.class)
                                       .withMetaInfo(instanceId)
                                       .build());
            return null;
        }

        return userData;
    }

    protected void validateMultipleEyesPrinciple(CloudTrailEvent event, String applicationId, String versionId,
            int minApprovals) {
        List<Approval> approvals = kioOperations.getApplicationApprovals(
                applicationId,
                versionId);
        List<String> approvalsFromMany = registryPluginProperties.getApprovalsFromMany();

        // #140
        // https://github.com/zalando-stups/fullstop/issues/140
        // => code, test and deploy approvals have to be done by at least two different people
        // e.g. four-eyes-principle
        int approverCount = approvals
                .stream()
                .filter(a -> approvalsFromMany.contains(a.getApprovalType()))
                .map(a -> a.getUserId())
                .distinct()
                .collect(Collectors.toList())
                .size();
        if (approverCount < minApprovals) {
            for (String instanceId : getInstanceIds(event)) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(VERSION_APPROVAL_NOT_ENOUGH).withPluginFullyQualifiedClassName(
                                RegistryPlugin.class).withMetaInfo(
                                newArrayList(
                                        versionId,
                                        applicationId,
                                        approverCount,
                                        minApprovals)).build());
            }
        }

    }

    protected void validateContainsMandatoryApprovals(Version version, CloudTrailEvent event) {
        List<Approval> approvals = kioOperations.getApplicationApprovals(
                version.getApplicationId(),
                version.getId());
        List<String> defaultApprovals = registryPluginProperties.getMandatoryApprovals();

        // #139
        // https://github.com/zalando-stups/fullstop/issues/139
        // does not have all default approval types
        Set<String> approvalTypes = approvals.stream()
                                             .collect(Collectors.groupingBy(Approval::getApprovalType))
                                             .keySet();
        if (!approvalTypes.containsAll(defaultApprovals)) {
            Set<String> diff = Sets.newHashSet(defaultApprovals);
            diff.removeAll(approvalTypes);
            for (String instanceId : getInstanceIds(event)) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(MISSING_VERSION_APPROVAL).withPluginFullyQualifiedClassName(
                                RegistryPlugin.class).withMetaInfo(
                                newArrayList(
                                        version.getId(),
                                        diff.toString(),
                                        version.getApplicationId())).build());
            }
        }
    }

    protected void validateScmSource(CloudTrailEvent event, String teamId, String applicationId,
            String applicationVersion) {
        Map<String, String> scmSource = newHashMap();
        try {
            scmSource = pieroneOperations.getScmSource(
                    teamId,
                    applicationId,
                    applicationVersion);
        }
        catch (HttpClientErrorException e) {
            for (String instanceId : getInstanceIds(event)) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(IMAGE_IN_PIERONE_NOT_FOUND)
                                           .withPluginFullyQualifiedClassName(
                                                   RegistryPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(
                                                           teamId,
                                                           applicationId,
                                                           applicationVersion))
                                           .build());
            }
            return;
        }
        if (scmSource.isEmpty()) {
            for (String instanceId : getInstanceIds(event)) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(SCM_SOURCE_JSON_MISSING_FOR_IMAGE)
                                           .withPluginFullyQualifiedClassName(
                                                   RegistryPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(
                                                           teamId,
                                                           applicationId,
                                                           applicationVersion))
                                           .build());
            }
        }
    }

    protected void validateSourceWithPierone(final CloudTrailEvent event, final String applicationId,
            final String applicationVersion, final String team, final String source, final String artifact) {
        List<String> instanceIds = getInstanceIds(event);
        if (!artifact.contains(source)) {
            for (String instanceId : instanceIds) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(APPLICATION_VERSION_HAS_NOT_A_VALID_ARTIFACT)
                                           .withPluginFullyQualifiedClassName(
                                                   RegistryPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(
                                                           applicationId,
                                                           applicationVersion))
                                           .build());
            }
        }

        Map<String, String> tags = newHashMap();
        try {
            tags = this.pieroneOperations.listTags(
                    team,
                    applicationId);
        }
        catch (HttpClientErrorException e) {
            LOG.warn(
                    "Could not get the tags for team {} and applicationId {}",
                    team,
                    applicationId,
                    e);
        }

        if (tags.isEmpty()) {
            for (String instanceId : instanceIds) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(SOURCE_NOT_PRESENT_IN_PIERONE)
                                           .withPluginFullyQualifiedClassName(
                                                   RegistryPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(source))
                                           .build());
            }
        }
        else {
            String value = tags.get(applicationVersion);
            if (value == null) {
                for (String instanceId : instanceIds) {
                    violationSink.put(
                            violationFor(event).withInstanceId(instanceId)
                                               .withType(SOURCE_NOT_PRESENT_IN_PIERONE)
                                               .withPluginFullyQualifiedClassName(
                                                       RegistryPlugin.class)
                                               .withMetaInfo(
                                                       newArrayList(source))
                                               .build());
                }
            }
        }
    }

    protected Application getAndValidateApplicationFromKio(final CloudTrailEvent event, final String applicationId) {

        try {
            Application application = kioOperations.getApplicationById(applicationId);
            return application;
        }
        catch (NotFoundException e) {
            for (String instanceId : getInstanceIds(event)) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(APPLICATION_NOT_PRESENT_IN_KIO)
                                           .withPluginFullyQualifiedClassName(
                                                   RegistryPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(applicationId))
                                           .build());
            }
            return null;
        }
        catch (HttpClientErrorException e) {
            LOG.warn(
                    "Error when trying to get Application {} from Kio",
                    applicationId,
                    e);
            return null;
        }

    }

    protected Version getAndValidateApplicationVersionFromKio(final CloudTrailEvent event, final String applicationId,
            final String applicationVersion) {

        try {
            return kioOperations.getApplicationVersion(
                    applicationId,
                    applicationVersion);
        }
        catch (NotFoundException e) {
            for (String instanceId : getInstanceIds(event)) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(APPLICATION_VERSION_NOT_PRESENT_IN_KIO)
                                           .withPluginFullyQualifiedClassName(
                                                   RegistryPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(
                                                           applicationId,
                                                           applicationVersion))
                                           .build());
            }
            return null;
        }
        catch (HttpClientErrorException e) {
            LOG.warn(
                    "Error when trying to get Application {} with Version {} from Kio. Exception: {}",
                    applicationId,
                    applicationVersion,
                    e.getMessage());
            return null;
        }

    }

    protected String getAndValidateApplicationId(final CloudTrailEvent event, final Map userDataMap,
            final String instanceId) {
        String applicationId = (String) userDataMap.get(APPLICATION_ID);

        if (applicationId == null) {
            violationSink.put(
                    violationFor(event).withInstanceId(instanceId)
                                       .withType(WRONG_USER_DATA)
                                       .withPluginFullyQualifiedClassName(
                                               RegistryPlugin.class)
                                       .withMetaInfo(
                                               newArrayList(instanceId))
                                       .build());
            return null;
        }

        return applicationId;
    }

    protected String getAndValidateApplicationVersion(final CloudTrailEvent event, final Map userDataMap,
            final String instanceId) {
        String applicationVersion = (String) userDataMap.get(APPLICATION_VERSION);

        if (applicationVersion == null) {
            violationSink.put(
                    violationFor(event).withInstanceId(instanceId)
                                       .withType(WRONG_USER_DATA)
                                       .withPluginFullyQualifiedClassName(
                                               RegistryPlugin.class)
                                       .withMetaInfo(
                                               newArrayList(instanceId))
                                       .build());
            return null;
        }

        return applicationVersion;
    }

    protected String getAndValidateSource(final CloudTrailEvent event, final Map userDataMap, final String instanceId) {

        String source = (String) userDataMap.get(SOURCE);

        if (source == null) {
            violationSink.put(
                    violationFor(event).withInstanceId(instanceId)
                                       .withType(WRONG_USER_DATA)
                                       .withPluginFullyQualifiedClassName(
                                               RegistryPlugin.class)
                                       .withMetaInfo(
                                               newArrayList(instanceId))
                                       .build());
            return null;
        }

        return source;
    }
}
