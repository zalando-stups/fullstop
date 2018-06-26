package org.zalando.stups.fullstop.web.model;

import io.swagger.annotations.ApiModel;
import org.zalando.stups.fullstop.violation.Violation;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

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


    public CreateViolation() {
    }


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
    @NotNull
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    @NotNull
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    @NotNull
    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Override
    @Nullable
    public String getPluginFullyQualifiedClassName() {
        return null;
    }

    @Override
    @Nullable
    public Boolean getChecked() {
        return null;
    }

    @Override
    @Nullable
    public String getComment() {
        return null;
    }
}
