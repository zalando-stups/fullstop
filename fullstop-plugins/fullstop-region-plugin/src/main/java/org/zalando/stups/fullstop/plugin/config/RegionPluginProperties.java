package org.zalando.stups.fullstop.plugin.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author  jbellmann
 */
@ConfigurationProperties(prefix = "fullstop.plugin.region")
public class RegionPluginProperties {

    private List<String> whitelistedRegions = new ArrayList<String>();

    public List<String> getWhitelistedRegions() {
        return whitelistedRegions;
    }

    public void setWhitelistedRegions(final List<String> whitelistedRegions) {
        this.whitelistedRegions = whitelistedRegions;
    }

}
