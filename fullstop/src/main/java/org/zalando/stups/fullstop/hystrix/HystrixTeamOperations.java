package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.HttpClientErrorException;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;

import java.util.List;
import java.util.Set;

public class HystrixTeamOperations implements TeamOperations {

    private final TeamOperations delegate;

    public HystrixTeamOperations(final TeamOperations delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = "aws-accounts-by-user", cacheManager = "oneMinuteTTLCacheManager")
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public List<Account> getAwsAccountsByUser(final String userId) {
        return delegate.getAwsAccountsByUser(userId);
    }

    @Override
    @Cacheable(cacheNames = "team-ids-by-user", cacheManager = "oneMinuteTTLCacheManager")
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public Set<String> getTeamIdsByUser(final String userId) {
        return delegate.getTeamIdsByUser(userId);
    }

    @Override
    @Cacheable(cacheNames = "active-aws-accounts", cacheManager = "oneMinuteTTLCacheManager")
    @HystrixCommand(ignoreExceptions = HttpClientErrorException.class)
    public List<Account> getActiveAccounts() {
        return delegate.getActiveAccounts();
    }

}
