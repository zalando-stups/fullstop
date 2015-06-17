package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * Created by gkneitschel.
 */

@Table(name = "application", schema = "fullstop_data")
@Entity
public class ApplicationEntity extends AbstractModifiableEntity {

    private String appName;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "app_has_version")

    public ApplicationEntity(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("appName", appName)
                .toString();
    }
}
