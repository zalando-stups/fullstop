package org.zalando.stups.fullstop.violation.domain;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.zalando.stups.fullstop.violation.domain.validation.groups.PersistenceOnly;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import static com.google.common.base.MoreObjects.ToStringHelper;

/**
 * @author ahartmann
 */
@MappedSuperclass
public abstract class AbstractModifiableEntity extends AbstractCreatableEntity {

    @LastModifiedBy
    @NotEmpty(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    protected String lastModifiedBy;

    /**
     * Enables optimistic locking.
     */
    @Version
    private Long version;

    @LastModifiedDate
    @NotNull(groups = { PersistenceOnly.class })
    @Column(nullable = false)
    private DateTime lastModified;

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(final String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy == null ? null : lastModifiedBy.trim();
    }

    @Override
    protected void addToStringFields(final ToStringHelper helper) {
        super.addToStringFields(helper);

        helper.add("lastModified", lastModified);
        helper.add("lastModifiedBy", lastModifiedBy);
        helper.add("version", version);
    }
}
