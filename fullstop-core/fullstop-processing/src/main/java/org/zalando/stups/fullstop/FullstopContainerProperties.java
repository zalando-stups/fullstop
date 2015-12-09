package org.zalando.stups.fullstop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jbellmann
 */
@ConfigurationProperties(prefix = "fullstop.container", ignoreUnknownFields = true)
public class FullstopContainerProperties {

    private boolean autoStart = true;

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(final boolean autoStart) {
        this.autoStart = autoStart;
    }

}
