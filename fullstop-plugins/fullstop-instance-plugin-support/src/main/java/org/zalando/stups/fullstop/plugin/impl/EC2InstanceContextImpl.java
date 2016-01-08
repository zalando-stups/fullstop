package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.model.Image;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.*;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.pierone.client.TagSummary;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getUsernameAsString;

public class EC2InstanceContextImpl implements EC2InstanceContext {

    private final String taupageNamePrefix;

    private final List<String> taupageOwners;

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

    private final PieroneTagProvider pieroneTagProvider;

    private final ScmSourceProvider scmSourceProvider;

    public EC2InstanceContextImpl(
            final CloudTrailEvent event,
            final String instanceJson,
            final ClientProvider clientProvider,
            final AmiIdProvider amiIdProvider,
            final AmiProvider amiProvider,
            final TaupageYamlProvider taupageYamlProvider,
            final String taupageNamePrefix,
            final List<String> taupageOwners,
            final KioApplicationProvider kioApplicationProvider,
            final KioVersionProvider kioVersionProvider,
            final KioApprovalProvider kioApprovalProvider,
            final PieroneTagProvider pieroneTagProvider,
            final ScmSourceProvider scmSourceProvider) {
        this.event = event;
        this.instanceJson = instanceJson;
        this.clientProvider = clientProvider;
        this.amiIdProvider = amiIdProvider;
        this.amiProvider = amiProvider;
        this.taupageYamlProvider = taupageYamlProvider;
        this.taupageNamePrefix = taupageNamePrefix;
        this.taupageOwners = taupageOwners;
        this.kioApplicationProvider = kioApplicationProvider;
        this.kioVersionProvider = kioVersionProvider;
        this.kioApprovalProvider = kioApprovalProvider;
        this.pieroneTagProvider = pieroneTagProvider;
        this.scmSourceProvider = scmSourceProvider;
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
        return JsonPath.read(getInstanceJson(), "$.instanceId");
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
        return getTaupageYaml().map(data -> (String) data.get("application_id")).map(StringUtils::trimToNull);
    }

    @Override
    public Optional<String> getVersionId() {
        return getTaupageYaml().map(data -> String.valueOf(data.get("application_version"))).map(StringUtils::trimToNull);
    }

    @Override
    public Optional<String> getSource() {
        return getTaupageYaml().map(data -> (String) data.get("source")).map(StringUtils::trimToNull);
    }

    @Override
    public Optional<String> getRuntime() {
        return getTaupageYaml().map(m -> (String) m.get("runtime")).map(StringUtils::trimToNull);
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
        return getAmi()
                .filter(image -> image.getName().startsWith(taupageNamePrefix))
                .map(Image::getOwnerId)
                .map(taupageOwners::contains);
    }

    @Override
    public Optional<Map> getTaupageYaml() {
        return taupageYamlProvider.apply(this);
    }

    @Override
    public Optional<TagSummary> getPieroneTag() {
        return pieroneTagProvider.apply(this);
    }

    @Override
    public Optional<Map<String, String>> getScmSource() {
        return scmSourceProvider.apply(this);
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
        // make sure to never add "expensive" information here.
        return toStringHelper(this)
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
