package org.zalando.stups.fullstop.teams;

import com.google.common.base.Preconditions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.RequestEntity.get;

public class RestTemplateTeamOperations implements TeamOperations {

    private final ParameterizedTypeReference<List<Account>> userTeamListType =
            new ParameterizedTypeReference<List<Account>>() {
            };

    private final ParameterizedTypeReference<List<Account>> accountType =
            new ParameterizedTypeReference<List<Account>>() {
            };

    private final RestOperations restOperations;

    private final String baseUrl;

    public RestTemplateTeamOperations(final RestOperations restOperations, final String baseUrl) {
        this.restOperations = restOperations;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<Account> getTeamsByUser(final String userId) {
        Preconditions.checkArgument(StringUtils.hasText(userId), "userId must not be blank");

        final ResponseEntity<List<Account>> response = restOperations.exchange(
                get(URI.create(baseUrl + "/api/accounts/aws?member=" + userId)).build(), userTeamListType);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful(), "getTeamsByUser failed: %s", response);
        return response.getBody();
    }

    @Override
    public List<Account> getActiveAccounts() {
        final ResponseEntity<List<Account>> response = restOperations.exchange(
                get(URI.create(baseUrl + "/api/accounts/aws")).build(), accountType);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful(), "getAccounts failed: %s", response);
        return response.getBody().parallelStream().filter(account -> !account.isDisabled()).collect(Collectors.toList());
    }
}
