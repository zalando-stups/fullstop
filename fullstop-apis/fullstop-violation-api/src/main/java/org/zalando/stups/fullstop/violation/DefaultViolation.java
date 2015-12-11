package org.zalando.stups.fullstop.violation;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author jbellmann
 */
class DefaultViolation implements Violation {

    private String eventId;

    private String accountId;

    private String region;

    private String instanceId;

    private Object metaInfo;

    private String comment;

    private Boolean checked;

    private String pluginFullyQualifiedClassName;

    private String violationType;

    private String username;

    DefaultViolation(final String eventId, final String accountId, final String region, final String instanceId, final String message,
            final Object metaInfo, final String comment, final Boolean checked, final String username) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.region = region;
        this.instanceId = instanceId;
        this.metaInfo = metaInfo;
        this.comment = comment;
        this.checked = checked;
        this.username = username;
    }

    DefaultViolation() {
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public Object getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(final Object metaInfo) {
        this.metaInfo = metaInfo;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Override
    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(final Boolean checked) {
        this.checked = checked;
    }

    @Override
    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    @Override
    public String getPluginFullyQualifiedClassName() {
        return pluginFullyQualifiedClassName;
    }

    public void setPluginFullyQualifiedClassName(String pluginFullyQualifiedClassName) {
        this.pluginFullyQualifiedClassName = pluginFullyQualifiedClassName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("eventId", eventId)
                .add("accountId", accountId)
                .add("region", region)
                .add("metaInfo", metaInfo)
                .add("comment", comment)
                .add("checked", checked)
                .add("pluginFullyQualifiedClassName", pluginFullyQualifiedClassName)
                .add("violationType", violationType)
                .add("username", username)
                .toString();
    }
}
