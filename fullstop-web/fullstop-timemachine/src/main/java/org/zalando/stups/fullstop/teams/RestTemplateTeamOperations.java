/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.teams;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.springframework.http.RequestEntity.get;

public class RestTemplateTeamOperations implements TeamOperations {

    private final ParameterizedTypeReference<List<Account>> userTeamListType =
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
        checkArgument(StringUtils.hasText(userId), "userId must not be blank");

        final ResponseEntity<List<Account>> response = restOperations.exchange(
                get(
                        URI.create(baseUrl + "/api/accounts/aws?member=" + userId)).build(), userTeamListType);
        checkState(response.getStatusCode().is2xxSuccessful(), "getTeamsByUser failed: %s", response);
        return response.getBody();
    }
}
