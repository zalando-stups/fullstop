package org.zalando.stups.fullstop.plugin;

import org.springframework.plugin.metadata.PluginMetadata;

/**
 * Base that can be used to implement a {@link FullstopPlugin}.
 *
 * @author jbellmann
 */
public abstract class AbstractFullstopPlugin implements FullstopPlugin {

    @Override
    public PluginMetadata getMetadata() {

        return new DefaultMetadataProvider(getClass().getName()).getMetadata();
    }

}
