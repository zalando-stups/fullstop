package org.zalando.stups.fullstop;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.EventsProcessor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import java.util.List;

/**
 * Simple {@link EventsProcessor} that delegates to {@link FullstopPlugin}s that can procces the {@link CloudTrailEvent}.
 */
@Component
public class PluginEventsProcessor implements EventsProcessor {

    private final Logger log = LoggerFactory.getLogger(PluginEventsProcessor.class);

    private final PluginRegistry<FullstopPlugin, CloudTrailEvent> fullstopPluginRegistry;

    @Autowired
    public PluginEventsProcessor(final PluginRegistry<FullstopPlugin, CloudTrailEvent> fullstopPluginRegistry) {
        this.fullstopPluginRegistry = fullstopPluginRegistry;
    }

    @Override
    public void process(final List<CloudTrailEvent> events) throws CallbackException {
        events.forEach(this::doProcess);
    }

    /**
     * Processes an single event by looping available plugins.
     *
     * @see #doProcess(CloudTrailEvent, FullstopPlugin)
     */
    protected void doProcess(final CloudTrailEvent event) {
        for (final FullstopPlugin plugin : getPluginsForEvent(event)) {
            doProcess(event, plugin);
        }
    }

    /**
     * Processes a specific event on specified plugin.
     */
    protected void doProcess(final CloudTrailEvent event, final FullstopPlugin plugin) {
        try {
            plugin.processEvent(event);
        } catch (HystrixRuntimeException | HttpServerErrorException e) {
            log.warn(e.getMessage(), e);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Returns a list of plugins configured.
     *
     * @return list of plugins configured
     */
    protected List<FullstopPlugin> getPluginsForEvent(final CloudTrailEvent event) {
        return this.fullstopPluginRegistry.getPluginsFor(event);
    }

}
