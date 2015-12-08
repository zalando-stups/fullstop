package org.zalando.stups.fullstop.plugin;

import org.junit.Test;
import org.springframework.plugin.metadata.MetadataProvider;
import org.springframework.plugin.metadata.PluginMetadata;

import static org.assertj.core.api.Assertions.assertThat;

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
