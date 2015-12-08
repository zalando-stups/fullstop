package org.zalando.stups.fullstop;

import com.amazonaws.services.cloudtrail.processinglibrary.AWSCloudTrailProcessingExecutor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

@Configuration
@EnableConfigurationProperties({ FullstopContainerProperties.class, CloudTrailProcessingLibraryProperties.class })
public class FullstopConfig {

    @Autowired
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> fullstopPluginRegistry;

    @Autowired
    private CloudTrailProcessingLibraryProperties cloudTrailsProcessingLibraryProperties;

    @Bean
    public PluginEventsProcessor pluginEventsProcessor() {
        return new PluginEventsProcessor(fullstopPluginRegistry);

    }

    /**
     * @return {@link AWSCloudTrailProcessingExecutor}
     */
    @Bean
    public AWSCloudTrailProcessingExecutor awsCloudTrailProcessingExecutor() {
        return new AWSCloudTrailProcessingExecutor.Builder(
                pluginEventsProcessor(),
                new ExtPropertiesFileConfiguration(cloudTrailsProcessingLibraryProperties.getAsProperties())).withProgressReporter(
                new NoOpsProgressReporter()).build();
    }
}
