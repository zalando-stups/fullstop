package org.zalando.stups.fullstop.plugin.unapproved.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author mrandi
 */
@ConfigurationProperties(prefix = "fullstop.plugins.unapprovedServicesAndRole")
public class UnapprovedServicesAndRoleProperties {

    private static final List<String> DEFAULT_EVENT_NAMES = newArrayList(
            "CreateRole",
            "DeleteRole",
            "AttachRolePolicy",
            "UpdateAssumeRolePolicy",
            "PutRolePolicy");

    private List<String> eventNames = newArrayList();

    public List<String> getEventNames() {
        if (eventNames.isEmpty()) {
            return DEFAULT_EVENT_NAMES;
        }

        return eventNames;
    }

    public void setEventNames(final List<String> eventNames) {
        this.eventNames = eventNames;
    }

}
