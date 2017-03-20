package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "fullstop.plugins.scm")
public class ScmRepositoryPluginProperties {

    private Map<String, String> hosts;

    public Map<String, String> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, String> hosts) {
        this.hosts = hosts;
    }
}
