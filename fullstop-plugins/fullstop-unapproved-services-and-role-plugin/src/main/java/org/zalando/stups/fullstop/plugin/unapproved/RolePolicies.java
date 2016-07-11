package org.zalando.stups.fullstop.plugin.unapproved;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class RolePolicies {

    private final Set<String> attachedPolicyNames;
    private final Set<String> inlinePolicyNames;
    private final String mainPolicy;

    public RolePolicies(Set<String> attachedPolicyNames, Set<String> inlinePolicyNames, String mainPolicy) {
        this.attachedPolicyNames = ImmutableSet.copyOf(attachedPolicyNames);
        this.inlinePolicyNames = ImmutableSet.copyOf(inlinePolicyNames);
        this.mainPolicy = mainPolicy;
    }

    public Set<String> getAttachedPolicyNames() {
        return attachedPolicyNames;
    }

    public Set<String> getInlinePolicyNames() {
        return inlinePolicyNames;
    }

    public String getMainPolicy() {
        return mainPolicy;
    }
}
