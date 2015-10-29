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
package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.amazonaws.regions.Regions.EU_WEST_1;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

@Component
class IdentityManagementDataSource {

    private final ClientProvider clientProvider;
    private final AccountIdSupplier allAccountIds;

    @Autowired
    IdentityManagementDataSource(final ClientProvider clientProvider, AccountIdSupplier allAccountIds) {
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
    }

    Map<String, List<User>> getUsersByAccount(){
        final Set<String> accounts = allAccountIds.get();
        final Map<String, List<User>> results = newHashMapWithExpectedSize(accounts.size());
        accounts.forEach(accountId -> results.put(accountId, getIAMClient(accountId).listUsers().getUsers()));
        return results;
    }

    Map<String, List<AccessKeyMetadata>> getAccessKeysByAccount() {
        final Set<String> accounts = allAccountIds.get();
        final Map<String, List<AccessKeyMetadata>> results = newHashMapWithExpectedSize(accounts.size());
        accounts.forEach(accountId -> results.put(accountId, getIAMClient(accountId).listAccessKeys().getAccessKeyMetadata()));
        return results;
    }

    private AmazonIdentityManagementClient getIAMClient(String accountId) {
        return clientProvider.getClient(AmazonIdentityManagementClient.class, accountId, Region.getRegion(EU_WEST_1));
    }
}
