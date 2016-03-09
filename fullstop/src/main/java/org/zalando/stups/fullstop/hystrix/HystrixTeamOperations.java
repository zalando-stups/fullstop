package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
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
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public List<Account> getAwsAccountsByUser(String userId) {
        return delegate.getAwsAccountsByUser(userId);
    }

    @Override
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public Set<String> getTeamIdsByUser(String userId) {
        return delegate.getTeamIdsByUser(userId);
    }

    @Override
    @HystrixCommand(ignoreExceptions = HttpClientErrorException.class)
    public List<Account> getActiveAccounts() {
        return delegate.getActiveAccounts();
    }

}
