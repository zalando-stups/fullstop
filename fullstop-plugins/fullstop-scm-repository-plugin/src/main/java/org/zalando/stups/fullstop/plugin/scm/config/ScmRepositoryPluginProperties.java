package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.zalando.stups.fullstop.plugin.scm.Provider;

import java.util.Map;

@ConfigurationProperties(prefix = "fullstop.plugins.scm")
public class ScmRepositoryPluginProperties {

    @NestedConfigurationProperty
    private Map<Provider, HostProperties> hosts;

    public Map<Provider, HostProperties> getHosts() {
        return hosts;
    }

    public void setHosts(Map<Provider, HostProperties> hosts) {
        this.hosts = hosts;
    }

    public static class HostProperties {
        private Map<String, String> allowedOwners;

        public Map<String, String> getAllowedOwners() {
            return allowedOwners;
        }

        public void setAllowedOwners(Map<String, String> allowedOwners) {
            this.allowedOwners = allowedOwners;
        }
    }
}
