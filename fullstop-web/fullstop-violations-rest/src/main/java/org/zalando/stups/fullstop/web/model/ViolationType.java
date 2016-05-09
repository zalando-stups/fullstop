package org.zalando.stups.fullstop.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import static com.google.common.base.MoreObjects.toStringHelper;

@ApiModel(description = "")
public class ViolationType {

    private String id = null;

    private String helpText = null;

    private Integer violationSeverity = null;

    private boolean isAuditRelevant;

    private Long version = null;

    private DateTime created = null;

    private String createdBy = null;

    private DateTime lastModified = null;

    private String lastModifiedBy = null;
    private Integer priority;

    @ApiModelProperty(value = "")
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(final String helpText) {
        this.helpText = helpText;
    }

    @ApiModelProperty(value = "")
    public Integer getViolationSeverity() {
        return violationSeverity;
    }

    public void setViolationSeverity(final Integer violationSeverity) {
        this.violationSeverity = violationSeverity;
    }

    @ApiModelProperty(value = "")
    public boolean isAuditRelevant() {
        return isAuditRelevant;
    }

    public void setIsAuditRelevant(final boolean isAuditRelevant) {
        this.isAuditRelevant = isAuditRelevant;
    }

    @ApiModelProperty(value = "")
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    @ApiModelProperty(value = "")
    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    @ApiModelProperty(value = "")
    public DateTime getCreated() {
        return created;
    }

    public void setCreated(final DateTime created) {
        this.created = created;
    }

    @ApiModelProperty(value = "")
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    @ApiModelProperty(value = "")
    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    @ApiModelProperty(value = "")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(final String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override public String toString() {
        return toStringHelper(this)
                .add("lastModifiedBy", lastModifiedBy)
                .add("lastModified", lastModified)
                .add("createdBy", createdBy)
                .add("created", created)
                .add("version", version)
                .add("isAuditRelevant", isAuditRelevant)
                .add("priority", priority)
                .add("violationSeverity", violationSeverity)
                .add("helpText", helpText)
                .add("id", id)
                .toString();
    }
}
