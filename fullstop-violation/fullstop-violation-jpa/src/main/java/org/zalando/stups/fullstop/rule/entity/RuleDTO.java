package org.zalando.stups.fullstop.rule.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

public class RuleDTO {
    @JsonProperty(value = "account_id")
    private String accountId;

    @JsonProperty(value = "region")
    private String region;

    @JsonProperty(value = "application_id")
    private String applicationId;

    @JsonProperty(value = "application_version")
    private String applicationVersion;

    @JsonProperty(value = "image_name")
    private String imageName;

    @JsonProperty(value = "image_owner")
    private String imageOwner;

    @JsonProperty(value = "reason")
    private String reason;

    @JsonProperty(value = "expiry_date")
    private DateTime expiryDate;

    @JsonProperty(value = "violation_type_entity")
    private String violationTypeEntity;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageOwner() {
        return imageOwner;
    }

    public void setImageOwner(String imageOwner) {
        this.imageOwner = imageOwner;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public DateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(DateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getViolationTypeEntity() {
        return violationTypeEntity;
    }

    public void setViolationTypeEntity(String violationTypeEntity) {
        this.violationTypeEntity = violationTypeEntity;
    }
}
