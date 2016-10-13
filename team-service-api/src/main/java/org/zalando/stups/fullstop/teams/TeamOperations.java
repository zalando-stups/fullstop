package org.zalando.stups.fullstop.teams;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

public interface TeamOperations {

    @Cacheable(cacheNames = "aws-accounts-by-user", cacheManager = "oneMinuteTTLCacheManager")
    List<Account> getAwsAccountsByUser(String userId);

    @Cacheable(cacheNames = "team-ids-by-user", cacheManager = "oneMinuteTTLCacheManager")
    Set<String> getTeamIdsByUser(String userId);

    @Cacheable(cacheNames = "active-aws-accounts", cacheManager = "oneMinuteTTLCacheManager")
    List<Account> getActiveAccounts();
}
