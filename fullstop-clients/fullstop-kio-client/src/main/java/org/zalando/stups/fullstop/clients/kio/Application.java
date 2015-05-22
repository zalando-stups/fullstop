package org.zalando.stups.fullstop.clients.kio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author  jbellmann
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application extends ApplicationBase {

    @JsonProperty("specification_url")
    private String specificationUrl;

    @JsonProperty("documentation_url")
    private String documentationUrl;

    @JsonProperty("scm_url")
    private String scmUrl;

    private boolean active;

    @JsonProperty("service_url")
    private String serviceUrl;

    private String description;

    public String getSpecificationUrl() {
        return specificationUrl;
    }

    public void setSpecificationUrl(final String specificationUrl) {
        this.specificationUrl = specificationUrl;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(final String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void setScmUrl(final String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
