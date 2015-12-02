package org.zalando.stups.fullstop.plugin;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RouteTable;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EC2InstanceContext {

    CloudTrailEvent getEvent();

    String getInstanceJson();

    String getInstanceId();

    Optional<Instance> getInstance();

    Optional<String> getAmiId();

    Optional<Image> getAmi();

    <T extends AmazonWebServiceClient> T getClient(Class<T> type);

    ViolationBuilder violation();

    String getEventName();

    String getAccountId();

    Region getRegion();

    String getRegionAsString();

    Optional<String> getApplicationId();

    Optional<String> getVersionId();

    Optional<Application> getKioApplication();

    Optional<Version> getKioVersion();

    Optional<List<Approval>> getKioApprovals();

    Optional<Boolean> isTaupageAmi();

    Optional<Map> getTaupageYaml();

    List<RouteTable> getRouteTables();
}
