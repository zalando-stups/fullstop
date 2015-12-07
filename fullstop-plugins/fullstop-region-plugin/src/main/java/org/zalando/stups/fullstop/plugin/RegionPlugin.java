package org.zalando.stups.fullstop.plugin;

import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.EC2_WITH_KEYPAIR;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_REGION;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

/**
 * @author gkneitschel
 */
@Component
public class RegionPlugin extends AbstractEC2InstancePlugin {

    private final Logger log = getLogger(getClass());

    private final ViolationSink violationSink;

    private final RegionPluginProperties regionPluginProperties;

    @Autowired
    public RegionPlugin(final EC2InstanceContextProvider contextProvider, final ViolationSink violationSink,
                        final RegionPluginProperties regionPluginProperties) {
        super(contextProvider);
        this.violationSink = violationSink;
        this.regionPluginProperties = regionPluginProperties;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        // It should only be possible to change the region in "RunInstances" events
        // So activating the plugin while processing these event types should be sufficient
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(final EC2InstanceContext ec2InstanceContext) {
        final List<String> allowedRegions = regionPluginProperties.getWhitelistedRegions();
        if (!allowedRegions.contains(ec2InstanceContext.getRegionAsString())) {
            violationSink.put(ec2InstanceContext.violation()
                    .withType(WRONG_REGION)
                    .withPluginFullyQualifiedClassName(RegionPlugin.class)
                    .withMetaInfo(singletonMap("allowed_regions", allowedRegions))
                    .build());
        }
    }
}
