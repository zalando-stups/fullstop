package org.zalando.stups.fullstop.web.model;

import io.swagger.annotations.ApiModel;
import org.zalando.stups.fullstop.violation.Violation;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class CreateViolation implements Violation {
    private String eventId;
    private String accountId ;
    private String region ;
    private Object metaInfo;
    private String violationType;
    private String instanceId;
    private String username;
    private String applicationId;
    private String applicationVersion;

    @Override
    @NotNull
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    @NotNull
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    @NotNull
    public String getRegion() {
        return region;
    }


    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    @NotNull
    public Object getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(Object metaInfo) {
        this.metaInfo = metaInfo;
    }

    @Override
    @NotNull
    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Override
    public String getPluginFullyQualifiedClassName() {
        return null;
    }

    @Override
    public Boolean getChecked() {
        return null;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateViolation violation = (CreateViolation) o;
        return Objects.equals(eventId, violation.eventId) &&
                Objects.equals(accountId, violation.accountId) &&
                Objects.equals(region, violation.region) &&
                Objects.equals(metaInfo, violation.metaInfo) &&
                Objects.equals(violationType, violation.violationType) &&
                Objects.equals(instanceId, violation.instanceId) &&
                Objects.equals(username, violation.username) &&
                Objects.equals(applicationId, violation.applicationId) &&
                Objects.equals(applicationVersion, violation.applicationVersion);
    }

    @Override
    public int hashCode() {

        return Objects.hash(eventId, accountId, region, metaInfo, violationType, instanceId, username, applicationId, applicationVersion);
    }
}
