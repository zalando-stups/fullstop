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
package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import java.util.Optional;

import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getEventId;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getUsernameAsString;

public class EC2InstanceContextImpl implements EC2InstanceContext {

    private final ClientProvider clientProvider;

    public EC2InstanceContextImpl(ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public Optional<String> getAmiId() {
        // TODO
        return Optional.empty();
    }

    @Override
    public <T extends AmazonWebServiceClient> T getClient(Class<T> type) {
        return clientProvider.getClient(type, getAccountId(), getRegion());
    }

    @Override
    public ViolationBuilder violation() {
        return new ViolationBuilder()
                .withAccountId(getAccountId())
                .withRegion(getRegion().getName())
                .withEventId(getEventId(getEvent()))
                .withInstanceId(getInstanceId())
                .withUsername(getUsernameAsString(getEvent()));
    }

    private String getAccountId() {
        // TODO
        return null;
    }

    private Region getRegion() {
        return CloudTrailEventSupport.getRegion(getEvent());
    }

    @Override
    public CloudTrailEvent getEvent() {
        // TODO
        return null;
    }

    @Override
    public String getInstanceId() {
        // TODO
        return null;
    }

    @Override
    public Optional<String> getAmiName() {
        // TODO
        return null;
    }
}
