package org.zalando.stups.fullstop.plugin;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.model.Image;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.taupage.TaupageYaml;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.pierone.client.TagSummary;

import java.util.Map;
import java.util.Optional;

public interface EC2InstanceContext {

    CloudTrailEvent getEvent();

    String getInstanceJson();

    String getInstanceId();

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

    /**
     * "source" attribute in Taupage yaml
     */
    Optional<String> getSource();

    Optional<String> getRuntime();

    Optional<Application> getKioApplication();

    Optional<Boolean> isTaupageAmi();

    Optional<TaupageYaml> getTaupageYaml();

    Optional<TagSummary> getPieroneTag();

    Optional<Map<String,String>> getScmSource();
}
