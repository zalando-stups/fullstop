package org.zalando.stups.fullstop.rule.entity;

import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zalando.stups.fullstop.domain.validation.groups.PersistenceOnly;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "rule_entity", schema = "fullstop_data")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountId;

    private String region;

    private String application_id;

    private String application_version;

    private String image_name;

    private String image_owner;

    private String reason;

    private String expiry_date;

    @ManyToOne
    private ViolationTypeEntity violationTypeEntity;

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

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getApplication_version() {
        return application_version;
    }

    public void setApplication_version(String application_version) {
        this.application_version = application_version;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_owner() {
        return image_owner;
    }

    public void setImage_owner(String image_owner) {
        this.image_owner = image_owner;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public ViolationTypeEntity getViolationTypeEntity() {
        return violationTypeEntity;
    }

    public void setViolationTypeEntity(ViolationTypeEntity violationTypeEntity) {
        this.violationTypeEntity = violationTypeEntity;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
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
                .add("violationTypeEntity", violationTypeEntity)
                .add("expiry_date", expiry_date)
                .add("reason", reason)
                .add("image_owner", image_owner)
                .add("image_name", image_name)
                .add("application_version", application_version)
                .add("application_id", application_id)
                .add("region", region)
                .add("accountId", accountId)
                .add("id", id)
                .toString();
    }
}
