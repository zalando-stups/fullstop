package org.zalando.stups.fullstop.teams;

import java.util.List;
import java.util.Set;

public interface TeamOperations {

    List<Account> getAwsAccountsByUser(String userId);

    Set<String> getTeamIdsByUser(String userId);

    List<Account> getActiveAccounts();
}
