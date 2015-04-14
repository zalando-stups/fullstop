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
package org.zalando.stups.fullstop.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.springframework.plugin.metadata.MetadataProvider;
import org.springframework.plugin.metadata.PluginMetadata;

public class DefaultMetadataProviderTest {

    @Test
    public void testMetadataProvider() {
        String pluginDescriptorName = getClass().getName();
        MetadataProvider provider = new DefaultMetadataProvider(pluginDescriptorName);
        PluginMetadata metadata = provider.getMetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getName()).isNotNull();
        assertThat(metadata.getName()).isNotEmpty();
        assertThat(metadata.getVersion()).isNotNull();
        assertThat(metadata.getName()).isEqualTo(pluginDescriptorName);
        assertThat(metadata.getVersion()).isNotEmpty();
        assertThat(metadata.getVersion()).isEqualTo("1.2.3");
        System.out.println(metadata.toString());
    }

    @Test
    public void testMetadataProviderNotFound() {
        String pluginDescriptorName = "NotFound";
        MetadataProvider provider = new DefaultMetadataProvider(pluginDescriptorName);
        PluginMetadata metadata = provider.getMetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getName()).isNotNull();
        assertThat(metadata.getName()).isNotEmpty();
        assertThat(metadata.getVersion()).isNotNull();
        assertThat(metadata.getName()).isEqualTo(pluginDescriptorName);
        assertThat(metadata.getVersion()).isNotEmpty();
        assertThat(metadata.getVersion()).isEqualTo("UNDEFINED");
        System.out.println(metadata.toString());
    }
}
