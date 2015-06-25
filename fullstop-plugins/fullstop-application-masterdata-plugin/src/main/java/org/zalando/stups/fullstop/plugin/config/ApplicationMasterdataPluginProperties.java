package org.zalando.stups.fullstop.plugin.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fullstop.plugins.amdp")
public class ApplicationMasterdataPluginProperties {

    private List<String> validatorsEnabled = new ArrayList<String>();

    public List<String> getValidatorsEnabled() {
        return validatorsEnabled;
    }

    public void setValidatorsEnabled(final List<String> validatorsEnabled) {
        this.validatorsEnabled = validatorsEnabled;
    }

}
