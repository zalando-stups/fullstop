package org.zalando.stups.fullstop.teams;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fullstop.clients.teamService")
public class TeamServiceProperties {

    /**
     * Comma-separated list of roles, that can be queried to verify if a user has access to some AWS account and is
     * authorized to resolve violations.
     */
    private String awsMembershipRoles = "PowerUser,Deployer";

    public String getAwsMembershipRoles() {
        return awsMembershipRoles;
    }

    public void setAwsMembershipRoles(String awsMembershipRoles) {
        this.awsMembershipRoles = awsMembershipRoles;
    }

    public String[] getAwsMembershipRolesAsArray() {
        return awsMembershipRoles.split(",");
    }
}
