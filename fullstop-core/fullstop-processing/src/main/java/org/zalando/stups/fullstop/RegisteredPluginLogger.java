package org.zalando.stups.fullstop;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

/**
 * @author jbellmann
 */
@Component
public class RegisteredPluginLogger {

    private final Logger log = LoggerFactory.getLogger(RegisteredPluginLogger.class);

    private final PluginRegistry<FullstopPlugin, CloudTrailEvent> fullstopPluginRegistry;

    @Autowired
    public RegisteredPluginLogger(final PluginRegistry<FullstopPlugin, CloudTrailEvent> fullstopPluginRegistry) {
        this.fullstopPluginRegistry = fullstopPluginRegistry;
    }

    public void logRegisteredPlugins() {
        log.info("---- REGISTERED PLUGINS START----");

        for (final FullstopPlugin p : this.fullstopPluginRegistry.getPlugins()) {
            log.info(p.getMetadata().toString());
        }

        log.info("---- REGISTERED PLUGINS END----");
    }
}
