package org.zalando.stups.differentnamespace;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;
import org.zalando.stups.fullstop.plugin.RegionPlugin;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;

import java.util.List;

import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FullstopApplication.class)
@IntegrationTest("debug=true")
@ActiveProfiles("triple")
public class FullstopApplicationTripleIT {

    @Autowired
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Autowired
    private RegionPlugin regionPlugin;

    @Autowired
    private RegionPluginProperties regionPluginProperties;

    @Test
    public void testRegionPlugin() {

        Assertions.assertThat(regionPluginProperties.getWhitelistedRegions()).containsOnly(
                "us-west-1", "us-east-1",
                "us-west-2");

        List<FullstopPlugin> plugins = pluginRegistry.getPlugins();
        Assertions.assertThat(plugins).isNotEmpty();
        Assertions.assertThat(plugins).contains(regionPlugin);

        CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/run-instance-us-west.json");

        for (FullstopPlugin plugin : plugins) {
            plugin.processEvent(cloudTrailEvent);
        }
    }
}
