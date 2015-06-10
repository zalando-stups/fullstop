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
package org.zalando.stups.fullstop;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.plugin.core.PluginRegistry;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.EventsProcessor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * Simple {@link EventsProcessor} that delegates to {@link FullstopPlugin}s that can procces the {@link CloudTrailEvent}.
 *
 * @author  jbellmann
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
        for (CloudTrailEvent event : events) {
            doProcess(event);
        }
    }

    /**
     * Processes an single event by looping available plugins.
     *
     * @param  event
     *
     * @see    #doProcess(CloudTrailEvent, FullstopPlugin)
     */
    protected void doProcess(final CloudTrailEvent event) {
        for (FullstopPlugin plugin : getPluginsForEvent(event)) {
            doProcess(event, plugin);
        }
    }

    /**
     * Processes an specific event on specified plugin.
     *
     * @param  event
     * @param  plugin
     */
    protected void doProcess(final CloudTrailEvent event, final FullstopPlugin plugin) {
        try {

            plugin.processEvent(event);
        } catch (Exception e) {

            // can we do more?
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Returns a list of plugins configured.
     *
     * @param   event
     *
     * @return  list of plugins configured
     */
    protected List<FullstopPlugin> getPluginsForEvent(final CloudTrailEvent event) {
        return this.fullstopPluginRegistry.getPluginsFor(event);
    }

}
