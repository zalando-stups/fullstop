package org.zalando.stups.fullstop.taupage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaupageYaml {

    private final String applicationId;
    private final String applicationVersion;
    private final String runtime;
    private final String source;

    @JsonCreator
    public TaupageYaml(
            @JsonProperty("application_id") final String applicationId,
            @JsonProperty("application_version") final String applicationVersion,
            @JsonProperty("runtime") final String runtime,
            @JsonProperty("source") final String source) {
        this.applicationId = applicationId;
        this.applicationVersion = applicationVersion;
        this.runtime = runtime;
        this.source = source;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getSource() {
        return source;
    }
}
