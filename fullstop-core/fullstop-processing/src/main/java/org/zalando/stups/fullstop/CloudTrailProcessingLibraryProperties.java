package org.zalando.stups.fullstop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Collects properties necessary for cloudtrailsprocessinglibrary.
 *
 * @author jbellmann
 */
@ConfigurationProperties(prefix = "fullstop.processor")
public class CloudTrailProcessingLibraryProperties {

    private Map<String, String> properties = new HashMap<String, String>();

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public Properties getAsProperties() {
        final Properties properties = new Properties();
        properties.putAll(getProperties());
        return properties;
    }

}
