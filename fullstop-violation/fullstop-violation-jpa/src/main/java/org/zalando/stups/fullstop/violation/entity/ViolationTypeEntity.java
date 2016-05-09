package org.zalando.stups.fullstop.violation.entity;

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

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by mrandi.
 */
@Table(name = "violation_type", schema = "fullstop_data")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ViolationTypeEntity {

    @Id
    private String id;

    private String helpText;

    private Integer violationSeverity;

    private boolean isAuditRelevant;

    private String title;

    private Integer priority;

    @CreatedDate
    @NotNull(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    private DateTime created;

    @CreatedBy
    @NotEmpty(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    private String createdBy;

    @LastModifiedDate
    @NotNull(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    private DateTime lastModified;

    @LastModifiedBy
    @NotEmpty(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    private String lastModifiedBy;

    /**
     * Enables optimistic locking.
     */
    @Version
    private Long version;

    public ViolationTypeEntity() {
    }

    public ViolationTypeEntity(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(final String helpText) {
        this.helpText = helpText;
    }

    public Integer getViolationSeverity() {
        return violationSeverity;
    }

    public void setViolationSeverity(
            final Integer violationSeverity) {
        this.violationSeverity = violationSeverity;
    }

    public boolean isAuditRelevant() {
        return isAuditRelevant;
    }

    public void setIsAuditRelevant(final boolean isAuditRelevant) {
        this.isAuditRelevant = isAuditRelevant;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
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
        this.createdBy = createdBy == null ? null : createdBy.trim();
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
        this.lastModifiedBy = lastModifiedBy == null ? null : lastModifiedBy.trim();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("helpText", helpText)
                .add("violationSeverity", violationSeverity)
                .add("isAuditRelevant", isAuditRelevant)
                .add("title", title)
                .add("createdBy", createdBy)
                .add("created", created)
                .add("lastModifiedBy", lastModifiedBy)
                .add("version", version)
                .add("lastModified", lastModified)
                .toString();
    }
}
