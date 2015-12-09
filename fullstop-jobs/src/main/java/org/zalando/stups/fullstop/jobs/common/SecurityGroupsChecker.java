package org.zalando.stups.fullstop.jobs.common;

import com.amazonaws.regions.Region;

import java.util.Collection;
import java.util.Set;

/**
 * Created by gkneitschel.
 */
public interface SecurityGroupsChecker {

    Set<String> check(Collection<String> groupIds, String account, Region region);

}
