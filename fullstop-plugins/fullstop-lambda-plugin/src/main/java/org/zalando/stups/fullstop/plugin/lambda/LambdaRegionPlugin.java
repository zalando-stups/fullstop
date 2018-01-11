package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;

import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.EMPTY;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.FUNCTION_NAME;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.getFunctionArn;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.getFunctionName;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.hasLambdaSource;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.isCreateFunctionEvent;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_REGION;

@Component
public class LambdaRegionPlugin extends AbstractFullstopPlugin {

    private final ViolationSink violationSink;
    private final RegionPluginProperties regionProps;

    @Autowired
    public LambdaRegionPlugin(ViolationSink violationSink, RegionPluginProperties regionProps) {
        this.violationSink = violationSink;
        this.regionProps = regionProps;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        return hasLambdaSource(event) && isCreateFunctionEvent(event);
    }

    @Override
    public void processEvent(CloudTrailEvent event) {
        final List<String> allowedRegions = regionProps.getWhitelistedRegions();
        final String awsRegion = event.getEventData().getAwsRegion();
        if (!allowedRegions.contains(awsRegion)) {
            violationSink.put(violationFor(event)
                    .withType(WRONG_REGION)
                    .withPluginFullyQualifiedClassName(this.getClass())
                    .withInstanceId(getFunctionArn(event).orElse(null))
                    .withMetaInfo(ImmutableMap.of(FUNCTION_NAME, getFunctionName(event).orElse(EMPTY)))
                    .build());
        }
    }
}
