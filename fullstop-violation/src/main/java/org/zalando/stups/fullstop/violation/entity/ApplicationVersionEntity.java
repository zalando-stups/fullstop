package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by gkneitschel.
 */
@Table(name = "app_version", schema = "fullstop_data")
@Entity
public class ApplicationVersionEntity extends AbstractModifiableEntity {
    private String applicationVersion;

    public ApplicationVersionEntity(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("applicationVersion", applicationVersion)
                .toString();
    }
}
