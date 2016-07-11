package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.regions.Region;

/**
 * Created by mrandi.
 */
public interface PolicyProvider {

    RolePolicies getRolePolicies(String roleName, Region region, String accountId);
}
