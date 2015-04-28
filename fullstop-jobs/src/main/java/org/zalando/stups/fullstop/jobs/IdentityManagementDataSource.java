/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.aws.ClientProvider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;
import com.amazonaws.services.identitymanagement.model.ListUsersResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author  jbellmann
 */
@Component
class IdentityManagementDataSource {

    private final Logger logger = LoggerFactory.getLogger(IdentityManagementDataSource.class);

    private final ClientProvider clientProvider;

    @Autowired
    IdentityManagementDataSource(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    List<Tuple<String, ListAccessKeysResult>> getListAccessKeysResultPerAccountWithTuple() {

        List<Tuple<String, ListAccessKeysResult>> result = newArrayList();

        for (String accountId : getAccountIds()) {
            AmazonIdentityManagementClient identityClient = clientProvider.getClient(
                    AmazonIdentityManagementClient.class, accountId, Region.getRegion(Regions.EU_WEST_1));
            if (identityClient != null) {

                result.add(new Tuple<String, ListAccessKeysResult>(accountId, identityClient.listAccessKeys()));
            } else {

                logger.error("Could not create 'AmazonIdentityManagementClient' for accountId : {}", accountId);
            }
        }

        return result;
    }

    List<Tuple<String, ListUsersResult>> getListUsersResultPerAccountWithTuple() {
        List<Tuple<String, ListUsersResult>> result = newArrayList();

        for (String accountId : getAccountIds()) {
            AmazonIdentityManagementClient identityClient = clientProvider.getClient(
                    AmazonIdentityManagementClient.class, accountId, Region.getRegion(Regions.EU_WEST_1));
            if (identityClient != null) {

                result.add(new Tuple<String, ListUsersResult>(accountId, identityClient.listUsers()));
            } else {

                logger.error("Could not create 'AmazonIdentityManagementClient' for accountId : {}", accountId);
            }
        }

        return result;
    }

    protected Set<String> getAccountIds() {

        return Sets.newHashSet(Lists.newArrayList("someAccountId"));
    }

}
