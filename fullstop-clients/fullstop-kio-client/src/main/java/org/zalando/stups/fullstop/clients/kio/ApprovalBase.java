package org.zalando.stups.fullstop.clients.kio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author  jbellmann
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApprovalBase {

    private String notes;

    @JsonProperty("approval_type")
    private String approvalType;

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public String getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(final String approvalType) {
        this.approvalType = approvalType;
    }
}
