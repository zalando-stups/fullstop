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

    private final PluginEventsProcessor pluginEventProcessor;

    public LocalPluginProcessor(final FullstopPlugin fullstopPlugin) {
        Assert.notNull(fullstopPlugin, "Plugin should never be null");

        final List<FullstopPlugin> plugins = Lists.newArrayList();
        plugins.add(fullstopPlugin);
        final PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry = SimplePluginRegistry.create(plugins);
        pluginEventProcessor = new PluginEventsProcessor(pluginRegistry);
    }

    public void processEvents(final InputStream is) throws CallbackException {
        final FileEventReader fer = new FileEventReader(pluginEventProcessor);
        fer.readEvents(is);
    }

}
