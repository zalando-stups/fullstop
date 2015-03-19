/*
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;

import com.amazonaws.services.cloudtrail.processinglibrary.AWSCloudTrailProcessingExecutor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

import org.zalando.stups.fullstop.plugin.FullstopPlugin;

@Configuration
@EnableConfigurationProperties({ FullstopContainerProperties.class, CloudTrailProcessingLibraryProperties.class })
public class FullstopConfig {

    @Autowired
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Autowired
    private CloudTrailProcessingLibraryProperties cloudTrailsProcessingLibraryProperties;

    @Bean
    public PluginEventsProcessor pluginEventsProcessor() {
        return new PluginEventsProcessor(pluginRegistry);

    }

    /**
     * @return  {@link AWSCloudTrailProcessingExecutor}
     */
    @Bean
    public AWSCloudTrailProcessingExecutor awsCloudTrailProcessingExecutor() {
        return new AWSCloudTrailProcessingExecutor.Builder(pluginEventsProcessor(),
                new ExtPropertiesFileConfiguration(cloudTrailsProcessingLibraryProperties.getAsProperties())).build();
    }
}
