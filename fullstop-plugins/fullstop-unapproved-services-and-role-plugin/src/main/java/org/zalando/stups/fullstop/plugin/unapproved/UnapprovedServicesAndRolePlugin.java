package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.SessionContext;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.SessionIssuer;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import io.fabric8.zjsonpatch.JsonDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.plugin.unapproved.config.UnapprovedServicesAndRoleProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getAccountId;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getRegion;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.MODIFIED_ROLE_OR_SERVICE;

@Component
public class UnapprovedServicesAndRolePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(UnapprovedServicesAndRolePlugin.class);

    private static final String EVENT_SOURCE = "iam.amazonaws.com";

    private final PolicyProvider policyProvider;

    private final ViolationSink violationSink;

    private final UnapprovedServicesAndRoleProperties unapprovedServicesAndRoleProperties;

    private final PolicyTemplatesProvider policyTemplatesProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonNode emptyArray;

    @Autowired
    public UnapprovedServicesAndRolePlugin(final PolicyProvider policyProvider,
                                           final ViolationSink violationSink,
                                           final PolicyTemplatesProvider policyTemplatesProvider,
                                           final UnapprovedServicesAndRoleProperties unapprovedServicesAndRoleProperties) {
        this.policyProvider = policyProvider;
        this.violationSink = violationSink;
        this.policyTemplatesProvider = policyTemplatesProvider;
        this.unapprovedServicesAndRoleProperties = unapprovedServicesAndRoleProperties;

        try {
            this.emptyArray = objectMapper.readTree("[]");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        final CloudTrailEventData cloudTrailEventData = event.getEventData();
        final String eventSource = cloudTrailEventData.getEventSource();

        return eventSource.equals(EVENT_SOURCE)
                && (unapprovedServicesAndRoleProperties.getEventNames().contains(cloudTrailEventData.getEventName()))
                && !isPerformedByAdmin(cloudTrailEventData)
                && (policyTemplatesProvider.getPolicyTemplateNames().contains(getRoleName(event)));
    }

    private boolean isPerformedByAdmin(CloudTrailEventData eventData) {
        return Optional.ofNullable(eventData)
                .map(CloudTrailEventData::getUserIdentity)
                .map(UserIdentity::getSessionContext)
                .map(SessionContext::getSessionIssuer)
                .map(SessionIssuer::getUserName)
                .filter(roleName -> roleName.equals(unapprovedServicesAndRoleProperties.getAdministrator()))
                .isPresent();
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        final String roleName = getRoleName(event);

        if (roleName == null) {
            LOG.info("Could not find roleName for event: {}, {} for account: {} and region: {}",
                    event.getEventData().getEventId(),
                    event.getEventData().getEventName(),
                    event.getEventData().getAccountId(),
                    event.getEventData().getAwsRegion());
            return;
        }

        final List<String> errorMessages = newArrayList();
        final RolePolicies rolePolicies = policyProvider.getRolePolicies(roleName, getRegion(event), getAccountId(event));
        final Set<String> attachedPolicyNames = rolePolicies.getAttachedPolicyNames();
        final Set<String> inlinePolicyNames = rolePolicies.getInlinePolicyNames();
        final Set<String> expectedInlinePolicyNames = Collections.singleton(roleName);
        final String policyTemplate = policyTemplatesProvider.getPolicyTemplate(roleName);
        final String policy = rolePolicies.getMainPolicy();

        if (!attachedPolicyNames.isEmpty()) {
            errorMessages.add("You MUST not attach additional policies to this role!");
        }

        if (!Objects.equals(inlinePolicyNames, expectedInlinePolicyNames)) {
            errorMessages.add("You MUST not change the inline policies of this role");
        }

        final JsonNode policyJson;
        final JsonNode templatePolicyJson;

        try {
            policyJson = objectMapper.readTree(policy);
            templatePolicyJson = objectMapper.readTree(policyTemplate);
        } catch (final IOException e) {
            LOG.warn("Could not read policy tree! For policy: {} and policy template:  {}", policy, policyTemplate);
            return;
        }

        final JsonNode templateToPolicyDiff = JsonDiff.asJson(templatePolicyJson, policyJson);
        final JsonNode policyToTemplateDiff = JsonDiff.asJson(policyJson, templatePolicyJson);

        if (!templateToPolicyDiff.equals(emptyArray)) {
            errorMessages.add("You MUST not change the main policy document! See diffs for details.");
        }

        if (!errorMessages.isEmpty()) {
            violationSink.put(
                    violationFor(event)
                            .withPluginFullyQualifiedClassName(UnapprovedServicesAndRolePlugin.class)
                            .withType(MODIFIED_ROLE_OR_SERVICE)
                            .withMetaInfo(ImmutableMap
                                    .builder()
                                    .put("role_name", roleName)
                                    .put("error_messages", errorMessages)
                                    .put("attached_policy_names", attachedPolicyNames)
                                    .put("inline_policy_names", inlinePolicyNames)
                                    .put("expected_inline_policy_names", expectedInlinePolicyNames)
                                    .put("template_to_policy_diff", templateToPolicyDiff)
                                    .put("policy_to_template_diff", policyToTemplateDiff)
                                    .build())
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
