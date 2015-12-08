package org.zalando.stups.fullstop.plugin.config;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author jbellmann
 */
public class RegionPluginPropertiesTest {

    @Test
    public void testDefaultRegions() {
        RegionPluginProperties properties = new RegionPluginProperties();
        Assertions.assertThat(properties.getWhitelistedRegions()).contains("eu-central-1", "eu-west-1");
    }
}
