package org.zalando.stups.fullstop.rule.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;
import org.zalando.stups.fullstop.domain.validation.groups.PersistenceOnly;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import com.google.common.base.MoreObjects;

@Table(name = "rule_entity", schema = "fullstop_data")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RuleEntity {

    @Id
    private String ruleName;

    private String accountId;

    @ManyToOne
    private ViolationTypeEntity violationTypeEntity;

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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public ViolationTypeEntity getViolationTypeEntity() {
        return violationTypeEntity;
    }

    public void setViolationTypeEntity(
            final ViolationTypeEntity violationTypeEntity) {
        this.violationTypeEntity = violationTypeEntity;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(final String ruleName) {
        this.ruleName = ruleName;
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
                .add("ruleName", ruleName)
                .add("accountId", accountId)
                .add("violationTypeEntity", violationTypeEntity)
                .add("created", created)
                .add("createdBy", createdBy)
                .add("lastModified", lastModified)
                .add("lastModifiedBy", lastModifiedBy)
                .add("version", version)
                .toString();
    }
}
