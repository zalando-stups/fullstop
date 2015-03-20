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

import java.util.List;
import java.util.concurrent.TimeUnit;

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

import com.amazonaws.services.cloudtrail.processinglibrary.AWSCloudTrailProcessingExecutor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * Only checks the configuration works. The {@link AWSCloudTrailProcessingExecutor} will not be started in this test.
 * This can be configured in the application-{profile}.yml file.
 *
 * @author  jbellmann
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
