/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.Lists;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.util.Assert;
import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.events.FileEventReader;

import java.io.InputStream;
import java.util.List;

/**
 * Support for testing single plugin with records from classpath.
 *
 * @author jbellmann
 */
public class LocalPluginProcessor {

    private PluginEventsProcessor pluginEventProcessor;

    public LocalPluginProcessor(final FullstopPlugin fullstopPlugin) {
        Assert.notNull(fullstopPlugin, "Plugin should never be null");

        List<FullstopPlugin> plugins = Lists.newArrayList();
        plugins.add(fullstopPlugin);
        PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry = SimplePluginRegistry.create(plugins);
        pluginEventProcessor = new PluginEventsProcessor(pluginRegistry);
    }

    public void processEvents(final InputStream is) throws CallbackException {
        FileEventReader fer = new FileEventReader(pluginEventProcessor);
        fer.readEvents(is, new TestCloudTrailLog());
    }

}
