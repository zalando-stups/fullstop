package org.zalando.stups.fullstop.clients.kio;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author  jbellmann
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Approval extends ApprovalBase {

    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("version_id")
    private String versionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("approved_at")
    private Date approvedAt;

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public Date getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(final Date approvedAt) {
        this.approvedAt = approvedAt;
    }
}
