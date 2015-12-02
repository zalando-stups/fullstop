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
