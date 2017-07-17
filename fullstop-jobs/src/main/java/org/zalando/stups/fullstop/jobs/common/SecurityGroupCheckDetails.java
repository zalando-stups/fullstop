package org.zalando.stups.fullstop.jobs.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.List;

public class SecurityGroupCheckDetails {

    private final String groupName;
    private final List<String> offendingRules;

    public SecurityGroupCheckDetails(String groupName, List<String> offendingRules) {
        this.groupName = groupName;
        this.offendingRules = offendingRules;
    }

    @JsonProperty("group_name")
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty("offending_rules")
    public List<String> getOffendingRules() {
        return offendingRules;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("groupName", groupName)
                .add("offendingRules", offendingRules)
                .toString();
    }
}
