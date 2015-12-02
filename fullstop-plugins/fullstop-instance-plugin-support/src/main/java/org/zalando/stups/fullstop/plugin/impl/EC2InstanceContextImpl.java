package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RouteTable;
import com.jayway.jsonpath.JsonPath;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.*;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getUsernameAsString;

public class EC2InstanceContextImpl implements EC2InstanceContext {

    private final String taupageNamePrefix;

    private final String taupageOwner;

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

    private final AmiProvider amiProvider;

    private final TaupageYamlProvider taupageYamlProvider;

    private final KioApplicationProvider kioApplicationProvider;

    private final KioVersionProvider kioVersionProvider;

    private final KioApprovalProvider kioApprovalProvider;

    public EC2InstanceContextImpl(
            final CloudTrailEvent event,
            final String instanceJson,
            final ClientProvider clientProvider,
            final AmiIdProvider amiIdProvider,
            final AmiProvider amiProvider,
            final TaupageYamlProvider taupageYamlProvider,
            final String taupageNamePrefix,
            final String taupageOwner,
            final KioApplicationProvider kioApplicationProvider,
            final KioVersionProvider kioVersionProvider,
            final KioApprovalProvider kioApprovalProvider) {
        this.event = event;
        this.instanceJson = instanceJson;
        this.clientProvider = clientProvider;
        this.amiIdProvider = amiIdProvider;
        this.amiProvider = amiProvider;
        this.taupageYamlProvider = taupageYamlProvider;
        this.taupageNamePrefix = taupageNamePrefix;
        this.taupageOwner = taupageOwner;
        this.kioApplicationProvider = kioApplicationProvider;
        this.kioVersionProvider = kioVersionProvider;
        this.kioApprovalProvider = kioApprovalProvider;
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
    public Optional<Instance> getInstance() {
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
        return getTaupageYaml().map(data -> (String) data.get("application_id"));
    }

    @Override
    public Optional<String> getVersionId() {
        return getTaupageYaml().map(data -> (String) data.get("application_version"));
    }

    @Override
    public Optional<Application> getKioApplication() {
        return kioApplicationProvider.apply(this);
    }

    @Override
    public Optional<Version> getKioVersion() {
        return kioVersionProvider.apply(this);
    }

    @Override
    public List<Approval> getKioApprovals() {
        return kioApprovalProvider.apply(this);
    }

    @Override
    public Optional<String> getAmiId() {
        return amiIdProvider.apply(this);
    }

    @Override
    public Optional<Image> getAmi() {
        return amiProvider.apply(this);
    }

    @Override
    public Optional<Boolean> isTaupageAmi() {
        return getAmi().map(image -> image.getName().startsWith(taupageNamePrefix) && image.getOwnerId().equals(taupageOwner));
    }

    @Override
    public Optional<Map> getTaupageYaml() {
        return taupageYamlProvider.apply(this);
    }

    @Override
    public List<RouteTable> getRouteTables() {
        // TODO: see SubnetPlugin for implementation hint
        return Collections.emptyList();
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
        return toStringHelper(this)
                .add("accountId", getAccountId())
                .add("region", getRegion())
                .add("eventId", getEventId())
                .add("eventName", getEventName())
                .add("instanceId", getInstanceId())
                .add("applicationId", getApplicationId())
                .add("kioApplication",getKioApplication())
                .add("kioApplicationVersion",getKioVersion())
                .add("kioApplicationApproval",getKioApprovals())
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
