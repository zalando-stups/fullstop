package org.zalando.stups.fullstop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@ConfigurationProperties(prefix = "fullstop.whitelist.allowedTeams")
public class RuleControllerProperties {

    private List<String> allowedTeams = newArrayList();

    public List<String> getAllowedTeams() {

        return allowedTeams; //TODO default values?
    }

    public void setAllowedTeams(final List<String> allowedTeams) {
        this.allowedTeams = allowedTeams;
    }

}
