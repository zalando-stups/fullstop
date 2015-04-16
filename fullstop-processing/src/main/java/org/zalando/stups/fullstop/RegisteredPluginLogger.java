/**
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.plugin.core.PluginRegistry;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * @author  jbellmann
 */
@Component
public class RegisteredPluginLogger {

    private final Logger log = LoggerFactory.getLogger(RegisteredPluginLogger.class);

    private final PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Autowired
    public RegisteredPluginLogger(final PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    public void logRegisteredPlugins() {
        log.info("---- REGISTERED PLUGINS START----");

        for (FullstopPlugin p : this.pluginRegistry.getPlugins()) {
            log.info(p.getMetadata().toString());
        }

        log.info("---- REGISTERED PLUGINS END----");
    }
}
