package org.zalando.stups.fullstop.violation.domain;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.zalando.stups.fullstop.violation.domain.validation.groups.PersistenceOnly;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import static com.google.common.base.MoreObjects.ToStringHelper;

/**
 * @author ahartmann
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractCreatableEntity extends AbstractEntity {

    @CreatedBy
    @NotEmpty(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    protected String createdBy;

    @CreatedDate
    @NotNull(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    private DateTime created;

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

    @Override
    protected void addToStringFields(final ToStringHelper helper) {
        helper.add("created", created);
        helper.add("createdBy", createdBy);
    }
}
