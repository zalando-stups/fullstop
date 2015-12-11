package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;

import javax.persistence.*;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by gkneitschel.
 */
@Table(name = "app_version", schema = "fullstop_data")
@Entity
public class VersionEntity extends AbstractModifiableEntity {

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "versionEntities")
    private List<ApplicationEntity> applicationEntities;

    public VersionEntity() {
    }

    public VersionEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApplicationEntity> getApplicationEntities() {
        if (applicationEntities == null) {
            applicationEntities = newArrayList();
        }
        return applicationEntities;
    }

    public void setApplicationEntities(
            List<ApplicationEntity> applicationEntities) {
        this.applicationEntities = applicationEntities;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VersionEntity that = (VersionEntity) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return !(applicationEntities != null ?
                !applicationEntities.equals(that.applicationEntities) :
                that.applicationEntities != null);

    }

    @Override public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (applicationEntities != null ? applicationEntities.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                          .add("name", name)
                          .toString();
    }
}
