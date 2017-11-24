package org.zalando.stups.fullstop.plugin.unapproved.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

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

    private Set<String> adminRoles = newHashSet("Shibboleth-Administrator", "OrganizationAccountAccessRole");

    public List<String> getEventNames() {
        if (eventNames.isEmpty()) {
            return DEFAULT_EVENT_NAMES;
        }

        return eventNames;
    }

    public void setEventNames(final List<String> eventNames) {
        this.eventNames = eventNames;
    }

    public Set<String> getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(Set<String> adminRoles) {
        this.adminRoles = adminRoles;
    }
}
