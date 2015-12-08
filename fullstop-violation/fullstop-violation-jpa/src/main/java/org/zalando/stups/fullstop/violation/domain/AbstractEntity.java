package org.zalando.stups.fullstop.violation.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import static com.google.common.base.MoreObjects.ToStringHelper;
import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author ahartmann
 */
@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String toString() {

        final ToStringHelper helper = toStringHelper(this);
        helper.add("id", id);

        addToStringFields(helper);

        return helper.toString();
    }

    protected abstract void addToStringFields(ToStringHelper helper);
}
