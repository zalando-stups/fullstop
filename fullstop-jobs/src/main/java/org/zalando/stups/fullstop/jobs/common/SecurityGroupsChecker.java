package org.zalando.stups.fullstop.jobs.common;

import com.amazonaws.regions.Region;

import java.util.Collection;
import java.util.Map;

public interface SecurityGroupsChecker {

    /**
     * Checks the given groups for security issues and returns a map of unsecured groups and their details.
     */
    Map<String, SecurityGroupCheckDetails> check(Collection<String> groupIds, String account, Region region);

}
