package org.zalando.stups.fullstop.clients.kio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author  jbellmann
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationBase {

    private String id;

    @JsonProperty("team_id")
    private String teamId;

    private String name;

    private String subtitle;

    @JsonProperty("service_url")
    private String serviceUrl;

    @JsonProperty("matched_description")
    private String matchedDescription;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(final String teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(final String subtitle) {
        this.subtitle = subtitle;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getMatchedDescription() {
        return matchedDescription;
    }

    public void setMatchedDescription(final String matchedDescription) {
        this.matchedDescription = matchedDescription;
    }

}
