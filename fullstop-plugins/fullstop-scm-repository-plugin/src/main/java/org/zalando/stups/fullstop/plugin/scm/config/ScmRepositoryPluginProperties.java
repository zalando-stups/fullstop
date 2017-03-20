package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.zalando.stups.fullstop.plugin.scm.Provider;

import java.util.Map;

@ConfigurationProperties(prefix = "fullstop.plugins.scm")
public class ScmRepositoryPluginProperties {

    private Map<Provider, Map<String, String>> hosts;

    public Map<Provider, Map<String, String>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<Provider, Map<String, String>> hosts) {
        this.hosts = hosts;
    }
}
