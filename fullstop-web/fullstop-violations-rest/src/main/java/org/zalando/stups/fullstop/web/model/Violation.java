package org.zalando.stups.fullstop.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import static com.google.common.base.MoreObjects.toStringHelper;

@ApiModel(description = "")
public class Violation {

    private Long id = null;

    private Long version = null;

    private String eventId = null;

    private String accountId = null;

    private String region = null;

    private Object metaInfo = null;

    private String comment = null;

    private String instanceId = null;

    private String pluginFullyQualifiedClassName = null;

    private ViolationType violationType = null;

    private DateTime created = null;

    private String createdBy = null;

    private DateTime lastModified = null;

    private String lastModifiedBy = null;

    private String username = null;

    private Long ruleID = null;

    private String applicationId;

    private String applicationVersion;


    @ApiModelProperty(value = "")
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    @ApiModelProperty(value = "")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    @ApiModelProperty(value = "")
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    @ApiModelProperty(value = "")
    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    @ApiModelProperty(value = "")
    public Object getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(final Object metaInfo) {
        this.metaInfo = metaInfo;
    }

    @ApiModelProperty(value = "")
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
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

    @ApiModelProperty(value = "")
    public String getPluginFullyQualifiedClassName() {
        return pluginFullyQualifiedClassName;
    }

    public void setPluginFullyQualifiedClassName(final String pluginFullyQualifiedClassName) {
        this.pluginFullyQualifiedClassName = pluginFullyQualifiedClassName;
    }

    @ApiModelProperty(value = "")
    public ViolationType getViolationType() {
        return violationType;
    }

    public void setViolationType(final ViolationType violationType) {
        this.violationType = violationType;
    }

    @ApiModelProperty(value = "")
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    @ApiModelProperty(value = "")
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @ApiModelProperty(value = "")
    public Long getRuleID() {
        return ruleID;
    }

    public void setRuleID(final Long ruleID) {
        this.ruleID = ruleID;
    }

    @ApiModelProperty(value = "")
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    @ApiModelProperty(value = "")
    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(final String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("version", version)
                .add("eventId", eventId)
                .add("accountId", accountId)
                .add("region", region)
                .add("metaInfo", metaInfo)
                .add("comment", comment)
                .add("instanceId", instanceId)
                .add("pluginFullyQualifiedClassName", pluginFullyQualifiedClassName)
                .add("violationType", violationType)
                .add("ruleID", ruleID)
                .add("username", username)
                .add("applicationId", applicationId)
                .add("applicationVersion", applicationVersion)
                .add("created", created)
                .add("createdBy", createdBy)
                .add("lastModified", lastModified)
                .add("lastModifiedBy", lastModifiedBy)
                .toString();
    }
}
