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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;

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
            Map userData = getAndValidateUserData(event,
                                                  instanceId);
            if (userData == null) {
                return;
            }

            String applicationId = getAndValidateApplicationId(event,
                                                               userData,
                                                               instanceId);

            String applicationVersion = getAndValidateApplicationVersion(event,
                                                                         userData,
                                                                         instanceId);

            String source = getAndValidateSource(event,
                                                 userData,
                                                 instanceId);

            if (applicationId != null) {

                Application applicationFromKio = getAndValidateApplicationFromKio(event,
                                                                                  applicationId);

                if (applicationFromKio != null) {

                    Version applicationVersionFromKio = getAndValidateApplicationVersionFromKio(event,
                                                                                                applicationId,
                                                                                                applicationVersion);

                    if (applicationVersionFromKio != null) {

                        validateSourceWithPierone(event,
                                                  applicationId,
                                                  applicationVersion,
                                                  applicationFromKio.getTeamId(),
                                                  source,
                                                  applicationVersionFromKio.getArtifact());

                        validateScmSource(event,
                                          applicationFromKio.getTeamId(),
                                          applicationId,
                                          applicationVersion);

                        validateContainsMandatoryApprovals(applicationVersionFromKio,
                                                           event);
                        validateMultipleEyesPrinciple(event,
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
        try {
            userData = userDataProvider.getUserData(event,
                                                    instanceId);
        }
        catch (AmazonServiceException ex) {
            LOG.error(ex.getMessage());
            violationSink.put(new ViolationBuilder(format("InstanceId: %s doesn't have any userData.",
                                                          instanceId))
                                                                      .withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            return null;
        }

        if (userData == null) {
            violationSink.put(new ViolationBuilder(format("InstanceId: %s doesn't have any userData.",
                                                          instanceId))
                                                                      .withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            return null;
        }

        if (userData.isEmpty()) {
            violationSink.put(new ViolationBuilder(format("InstanceId: %s has empty userData.",
                                                          instanceId))
                                                                      .withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            return null;
        }

        return userData;
    }

    protected void validateMultipleEyesPrinciple(CloudTrailEvent event, String applicationId, String versionId,
            int minApprovals) {
        List<Approval> approvals = kioOperations.getApplicationApprovals(applicationId,
                                                                         versionId);
        Set<String> approvalsFromMany = registryPluginProperties.getApprovalsFromMany();

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
            violationSink
                         .put(new ViolationBuilder(
                                                   format("Version %s of application %s was approved by only %s people instead of %s.",
                                                          versionId,
                                                          applicationId,
                                                          approverCount,
                                                          minApprovals))
                                                                        .withAccountId(getCloudTrailEventAccountId(event))
                                                                        .withRegion(getCloudTrailEventRegion(event))
                                                                        .withEventId(getCloudTrailEventId(event))
                                                                        .build());
        }

    }

    protected void validateContainsMandatoryApprovals(Version version, CloudTrailEvent event) {
        List<Approval> approvals = kioOperations.getApplicationApprovals(version.getApplicationId(),
                                                                         version.getId());
        Set<String> defaultApprovals = registryPluginProperties.getMandatoryApprovals();

        // #139
        // https://github.com/zalando-stups/fullstop/issues/139
        // does not have all default approval types
        Set<String> approvalTypes = approvals.stream()
                                             .collect(Collectors.groupingBy(Approval::getApprovalType))
                                             .keySet();
        if (!approvalTypes.containsAll(defaultApprovals)) {
            Set<String> diff = Sets.newHashSet(defaultApprovals);
            diff.removeAll(approvalTypes);
            violationSink.put(new ViolationBuilder(format("Version %s of application %s is missing approvals: %s.",
                                                          version.getId(),
                                                          diff.toString(),
                                                          version.getApplicationId()))
                                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                                      .withEventId(getCloudTrailEventId(event))
                                                                                      .build());
        }
    }

    protected void validateScmSource(CloudTrailEvent event, String teamId, String applicationId,
            String applicationVersion) {
        Map<String, String> scmSource = Maps.newHashMap();
        try {
            scmSource = pieroneOperations.getScmSource(teamId,
                                                       applicationId,
                                                       applicationVersion);
        }
        catch (HttpClientErrorException e) {
            violationSink.put(new ViolationBuilder(format(
                                                          "Image for team: %s and application: %s with version: %s not found in pierone.",
                                                          teamId,
                                                          applicationId,
                                                          applicationVersion)).withEventId(getCloudTrailEventId(event))
                                                                              .withRegion(getCloudTrailEventRegion(event))
                                                                              .withAccountId(getCloudTrailEventAccountId(event))
                                                                              .build());
            return;
        }
        if (scmSource.isEmpty()) {
            violationSink.put(new ViolationBuilder(format(
                                                          "Image for team: %s and application: %s with version: %s does not contain scm-source.json.",
                                                          teamId,
                                                          applicationId,
                                                          applicationVersion)).withEventId(getCloudTrailEventId(event))
                                                                              .withRegion(getCloudTrailEventRegion(event))
                                                                              .withAccountId(getCloudTrailEventAccountId(event))
                                                                              .build());
        }
    }

    protected void validateSourceWithPierone(final CloudTrailEvent event, final String applicationId,
            final String applicationVersion, final String team, final String source, final String artifact) {

        if (!artifact.contains(source)) {
            violationSink.put(new ViolationBuilder(format("Application: %s has not a valid artifact for version: %s.",
                                                          applicationId,
                                                          applicationVersion)).withEventId(getCloudTrailEventId(event))
                                                                              .withRegion(getCloudTrailEventRegion(event))
                                                                              .withAccountId(getCloudTrailEventAccountId(event))
                                                                              .build());
        }

        Map<String, String> tags = Maps.newHashMap();
        try {
            tags = this.pieroneOperations.listTags(team,
                                                   applicationId);
        }
        catch (HttpClientErrorException e) {
            LOG.warn("Could not get the tags for team {} and applicationId {}",
                     team,
                     applicationId,
                     e);
        }

        if (tags.isEmpty()) {
            violationSink.put(new ViolationBuilder(format("Source: %s is not present in pierone.",
                                                          source))
                                                                  .withEventId(getCloudTrailEventId(event))
                                                                  .withRegion(getCloudTrailEventRegion(event))
                                                                  .withAccountId(getCloudTrailEventAccountId(event))
                                                                  .build());
        }
        else {
            String value = tags.get(applicationVersion);
            if (value == null) {
                violationSink.put(new ViolationBuilder(format("Source: %s is not present in pierone.",
                                                              source))
                                                                      .withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            }
        }
    }

    protected Application getAndValidateApplicationFromKio(final CloudTrailEvent event, final String applicationId) {

        try {
            Application application = kioOperations.getApplicationById(applicationId);
            return application;
        }
        catch (NotFoundException e) {
            violationSink.put(new ViolationBuilder(format("Application: %s is not present in kio.",
                                                          applicationId))
                                                                         .withEventId(getCloudTrailEventId(event))
                                                                         .withRegion(getCloudTrailEventRegion(event))
                                                                         .withAccountId(getCloudTrailEventAccountId(event))
                                                                         .build());
            return null;
        }
        catch (HttpClientErrorException e) {
            LOG.warn("Error when trying to get Application {} from Kio",
                     applicationId,
                     e);
            return null;
        }

    }

    protected Version getAndValidateApplicationVersionFromKio(final CloudTrailEvent event, final String applicationId,
            final String applicationVersion) {
        try {

            Version version = kioOperations.getApplicationVersion(applicationId,
                                                                  applicationVersion);
            return version;
        }
        catch (NotFoundException e) {
            violationSink.put(new ViolationBuilder(format("Application: %s is not present with version %s in kio.",
                                                          applicationId,
                                                          applicationVersion)).withEventId(getCloudTrailEventId(event))
                                                                              .withRegion(getCloudTrailEventRegion(event))
                                                                              .withAccountId(getCloudTrailEventAccountId(event))
                                                                              .build());
            return null;
        }
        catch (HttpClientErrorException e) {
            LOG.warn("Error when trying to get Application {} with Version {} from Kio",
                     applicationId,
                     applicationVersion,
                     e);
            return null;
        }

    }

    protected String getAndValidateApplicationId(final CloudTrailEvent event, final Map userDataMap,
            final String instanceId) {
        String applicationId = (String) userDataMap.get(APPLICATION_ID);

        if (applicationId == null) {
            violationSink.put(new ViolationBuilder(format("No 'application_id' defined for this instance %s, "
                                                                  + "please change the userData configuration for this instance and add this information.",
                                                          instanceId)).withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            return null;
        }

        return applicationId;
    }

    protected String getAndValidateApplicationVersion(final CloudTrailEvent event, final Map userDataMap,
            final String instanceId) {
        String applicationVersion = (String) userDataMap.get(APPLICATION_VERSION);

        if (applicationVersion == null) {
            violationSink.put(new ViolationBuilder(format("No 'application_version' defined for this instance %s, "
                                                                  + "please change the userData configuration for this instance and add this information.",
                                                          instanceId)).withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            return null;
        }

        return applicationVersion;
    }

    protected String getAndValidateSource(final CloudTrailEvent event, final Map userDataMap, final String instanceId) {

        String source = (String) userDataMap.get(SOURCE);

        if (source == null) {
            violationSink.put(new ViolationBuilder(format("No 'source' defined for this instance %s, "
                                                                  + "please change the userData configuration for this instance and add this information.",
                                                          instanceId)).withEventId(getCloudTrailEventId(event))
                                                                      .withRegion(getCloudTrailEventRegion(event))
                                                                      .withAccountId(getCloudTrailEventAccountId(event))
                                                                      .build());
            return null;
        }

        return source;
    }
}
