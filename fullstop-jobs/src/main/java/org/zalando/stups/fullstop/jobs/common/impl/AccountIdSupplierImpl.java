package org.zalando.stups.fullstop.jobs.common.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class AccountIdSupplierImpl implements AccountIdSupplier {

    private final TeamOperations teams;

    @Autowired
    public AccountIdSupplierImpl(final TeamOperations teams) {
        this.teams = teams;
    }

    @Override
    public Set<String> get() {
        return teams.getAccounts().stream().map(Account::getId).collect(toSet());
    }
}
