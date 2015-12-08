package org.zalando.stups.fullstop.plugin.config;

import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jbellmann
 */
@ConfigurationProperties(prefix = "fullstop.plugins.region")
public class RegionPluginProperties {

    private static final List<String> DEFAULT_REGIONS = Lists.newArrayList("eu-central-1", "eu-west-1");

    private List<String> whitelistedRegions = new ArrayList<String>();

    public List<String> getWhitelistedRegions() {
        if (whitelistedRegions.isEmpty()) {
            return DEFAULT_REGIONS;
        }

        return whitelistedRegions;
    }

    public void setWhitelistedRegions(final List<String> whitelistedRegions) {
        this.whitelistedRegions = whitelistedRegions;
    }

}
