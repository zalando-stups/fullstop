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
import com.google.common.base.MoreObjects;
import com.jayway.jsonpath.JsonPath;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.AmiIdProvider;
import org.zalando.stups.fullstop.plugin.provider.AmiNameProvider;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getUsernameAsString;

public class EC2InstanceContextImpl implements EC2InstanceContext {

    /**
     * The original CloudTrailEvent
     */
    private final CloudTrailEvent event;

    /**
     * An excerpt of the CloudTrailEvent for this particular instance.
     * In other words: one "item" in the responseElements $.instancesSet.items.
     */
    private final String instanceJson;

    private final ClientProvider clientProvider;

    private final AmiIdProvider amiIdProvider;

    private final AmiNameProvider amiNameProvider;

    public EC2InstanceContextImpl(
            final CloudTrailEvent event,
            final String instanceJson,
            final ClientProvider clientProvider,
            final AmiIdProvider amiIdProvider, AmiNameProvider amiNameProvider) {
        this.event = event;
        this.instanceJson = instanceJson;
        this.clientProvider = clientProvider;
        this.amiIdProvider = amiIdProvider;
        this.amiNameProvider = amiNameProvider;
    }

    @Override
    public CloudTrailEvent getEvent() {
        return event;
    }

    @Override
    public String getInstanceJson() {
        return instanceJson;
    }

    @Override
    public String getInstanceId() {
        return JsonPath.read(instanceJson, "$.instanceId");
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
                .withEventId(getEventId().toString())
                .withInstanceId(getInstanceId())
                .withUsername(getUsernameAsString(getEvent()));
    }

    @Override
    public String getRegionAsString() {
        return CloudTrailEventSupport.getRegionAsString(getEvent());
    }

    @Override
    public Region getRegion() {
        return CloudTrailEventSupport.getRegion(getEvent());
    }

    @Override
    public Optional<String> getApplicationId() {
        // TODO
        return null;
    }

    @Override
    public Optional<String> getVersionId() {
        // TODO
        return null;
    }

    @Override
    public Optional<String> getAmiId() {
        return amiIdProvider.apply(this);
    }

    @Override
    public Optional<String> getAmiName() {
        return amiNameProvider.apply(this);
    }

    @Override
    public String getEventName() {
        return getEvent().getEventData().getEventName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EC2InstanceContextImpl that = (EC2InstanceContextImpl) o;
        return Objects.equals(getEvent(), that.getEvent()) &&
                Objects.equals(getInstanceJson(), that.getInstanceJson());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEvent(), getInstanceJson());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", getAccountId())
                .add("region", getRegion())
                .add("eventId", getEventId())
                .add("eventName", getEventName())
                .add("instanceId", getInstanceId())
                .toString();
    }

    @Override
    public String getAccountId() {
        return getEvent().getEventData().getAccountId();
    }

    private UUID getEventId() {
        return getEvent().getEventData().getEventId();
    }
}
