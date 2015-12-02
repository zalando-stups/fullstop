package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.Account;

import java.util.List;

public class HystrixTeamOperations implements TeamOperations {

    private final TeamOperations delegate;

    public HystrixTeamOperations(final TeamOperations delegate) {
        this.delegate = delegate;
    }

    @Override
    @HystrixCommand
    public List<Account> getTeamsByUser(String userId) {
        return delegate.getTeamsByUser(userId);
    }

    @Override public List<Account> getAccounts() {
        return delegate.getAccounts();
    }

}
