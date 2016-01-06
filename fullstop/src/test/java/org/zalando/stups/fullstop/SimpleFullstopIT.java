package org.zalando.stups.fullstop;

import com.amazonaws.services.cloudtrail.processinglibrary.AWSCloudTrailProcessingExecutor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Only checks the configuration works. The {@link AWSCloudTrailProcessingExecutor} will not be started in this test.
 * This can be configured in the application-{profile}.yml file.
 *
 * @author jbellmann
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Fullstop.class)
@IntegrationTest
@ActiveProfiles("integration")
public class SimpleFullstopIT {

    @Autowired
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Test
    public void pluginRegistryAvailable() throws InterruptedException {
        Assertions.assertThat(pluginRegistry).isNotNull();

        List<FullstopPlugin> listOfPlugins = pluginRegistry.getPlugins();

        // be aware that we have the example-plugin always registered
        Assertions.assertThat(listOfPlugins.size()).isGreaterThanOrEqualTo(1);

        TimeUnit.MINUTES.sleep(20);
    }
}
