package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.plugin.unapproved.config.UnapprovedServicesAndRoleProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.io.IOException;

import static java.util.Collections.singletonMap;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;
import static org.zalando.stups.fullstop.violation.ViolationType.MODIFIED_ROLE_OR_SERVICE;

@Component
public class UnapprovedServicesAndRolePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(UnapprovedServicesAndRolePlugin.class);

    private static final String EVENT_SOURCE = "iam.amazonaws.com";

    private final PolicyProvider policyProvider;

    private final ViolationSink violationSink;

    private final UnapprovedServicesAndRoleProperties unapprovedServicesAndRoleProperties;

    private final PolicyTemplatesProvider policyTemplatesProvider;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UnapprovedServicesAndRolePlugin(final PolicyProvider policyProvider,
                                           final ViolationSink violationSink,
                                           final PolicyTemplatesProvider policyTemplatesProvider,
                                           final UnapprovedServicesAndRoleProperties unapprovedServicesAndRoleProperties) {
        this.policyProvider = policyProvider;
        this.violationSink = violationSink;
        this.policyTemplatesProvider = policyTemplatesProvider;
        this.unapprovedServicesAndRoleProperties = unapprovedServicesAndRoleProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();

        return eventSource.equals(EVENT_SOURCE)
                && (unapprovedServicesAndRoleProperties.getEventNames().contains(cloudTrailEventData.getEventName()))
                && (policyTemplatesProvider.getPolicyTemplateNames().contains(getRoleName(event)));
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        String roleName = getRoleName(event);

        if (roleName == null) {
            LOG.info("Could not find roleName for event: {}, {} for account: {} and region: {}",
                    event.getEventData().getEventId(),
                    event.getEventData().getEventName(),
                    event.getEventData().getAccountId(),
                    event.getEventData().getAwsRegion());
            return;
        }

        String policy = policyProvider.getPolicy(roleName, getRegion(event), getAccountId(event));

        String policyTemplate = policyTemplatesProvider.getPolicyTemplate(roleName);

        JsonNode policyJson;
        JsonNode templatePolicyJson;

        try {
            policyJson = objectMapper.readTree(policy);
            templatePolicyJson = objectMapper.readTree(policyTemplate);
        } catch (IOException e) {
            LOG.warn("Could not read policy tree! For policy: {} and policy template:  {}", policy, policyTemplate);
            return;
        }

        if (!policyJson.equals(templatePolicyJson)) {
            violationSink.put(
                    violationFor(event)
                            .withPluginFullyQualifiedClassName(UnapprovedServicesAndRolePlugin.class)
                            .withType(MODIFIED_ROLE_OR_SERVICE)
                            .withMetaInfo(singletonMap("role_name", roleName))
                            .build());

        }

    }

    private String getRoleName(final CloudTrailEvent event) {

        if (event.getEventData() != null
                && event.getEventData().getRequestParameters() != null
                && !event.getEventData().getRequestParameters().isEmpty()) {

            return JsonPath.read(event.getEventData().getRequestParameters(), "$.roleName");

        } else {
            return null;
        }

    }

}
