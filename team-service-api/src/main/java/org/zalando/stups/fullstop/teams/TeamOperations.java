package org.zalando.stups.fullstop.teams;

import java.util.List;

public interface TeamOperations {

    List<Account> getTeamsByUser(String userId);

    List<Account> getActiveAccounts();
}
