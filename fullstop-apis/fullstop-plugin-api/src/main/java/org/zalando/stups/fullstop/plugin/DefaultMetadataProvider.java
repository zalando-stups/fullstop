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

import org.springframework.plugin.metadata.MetadataProvider;
import org.springframework.plugin.metadata.PluginMetadata;
import org.springframework.plugin.metadata.SimplePluginMetadata;

import java.util.Properties;

/**
 * Reads metadata of plugin from classpath.
 *
 * @author jbellmann
 */
public class DefaultMetadataProvider implements MetadataProvider {

    private static final String PROPERTY_NAME = "version";

    private static final String UNDEFINED = "UNDEFINED";

    private static final String META_INF_FULLSTOP = "/META-INF/fullstop/";

    private final String pluginDescriptorName;

    public DefaultMetadataProvider(final String pluginDescriptorName) {
        this.pluginDescriptorName = pluginDescriptorName;
    }

    @Override
    public PluginMetadata getMetadata() {
        String name = pluginDescriptorName;
        String version = readVersion();
        return new SimplePluginMetadata(name, version);
    }

    protected String readVersion() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream(META_INF_FULLSTOP + getPluginDescriptorName()));

            Object version = properties.get(PROPERTY_NAME);
            if (version != null) {
                return version.toString();
            }

            return UNDEFINED;
        }
        catch (Exception e) {
            return UNDEFINED;
        }
    }

    protected String getPluginDescriptorName() {
        return pluginDescriptorName;
    }

}
