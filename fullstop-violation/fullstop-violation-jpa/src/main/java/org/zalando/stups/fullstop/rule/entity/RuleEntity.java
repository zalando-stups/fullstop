package org.zalando.stups.fullstop.rule.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zalando.stups.fullstop.domain.validation.groups.PersistenceOnly;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "rule", schema = "fullstop_data")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @JsonProperty(value = "violation_type_entity_id")
    private String violationTypeEntityId;

    @JsonProperty(value = "meta_data_json_path")
    private String metaInfoJsonPath;

    @CreatedDate
    @NotNull(groups = {PersistenceOnly.class})
    @Column(nullable = false)
    private DateTime created;

    @CreatedBy
    @NotEmpty(groups = {PersistenceOnly.class})
    @Column(nullable = false)
    private String createdBy;

    @LastModifiedDate
    @NotNull(groups = {PersistenceOnly.class})
    @Column(nullable = false)
    private DateTime lastModified;

    @LastModifiedBy
    @NotEmpty(groups = {PersistenceOnly.class})
    @Column(nullable = false)
    private String lastModifiedBy;

    /**
     * Enables optimistic locking.
     */
    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(final String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(final String imageName) {
        this.imageName = imageName;
    }

    public String getImageOwner() {
        return imageOwner;
    }

    public void setImageOwner(final String imageOwner) {
        this.imageOwner = imageOwner;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public DateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final DateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getViolationTypeEntityId() {
        return violationTypeEntityId;
    }

    public void setViolationTypeEntityId(final String violationTypeEntityId) {
        this.violationTypeEntityId = violationTypeEntityId;
    }

    public String getMetaInfoJsonPath() {
        return metaInfoJsonPath;
    }

    public void setMetaInfoJsonPath(String metaInfoJsonPath) {
        this.metaInfoJsonPath = metaInfoJsonPath;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(final DateTime created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(final String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", version)
                .add("lastModifiedBy", lastModifiedBy)
                .add("lastModified", lastModified)
                .add("createdBy", createdBy)
                .add("created", created)
                .add("metaInfoJsonPath", metaInfoJsonPath)
                .add("violationTypeEntityId", violationTypeEntityId)
                .add("expiryDate", expiryDate)
                .add("reason", reason)
                .add("imageOwner", imageOwner)
                .add("imageName", imageName)
                .add("applicationVersion", applicationVersion)
                .add("applicationId", applicationId)
                .add("region", region)
                .add("accountId", accountId)
                .add("id", id)
                .toString();
    }
}
