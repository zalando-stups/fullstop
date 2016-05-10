package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;

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

    public VersionEntity(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<ApplicationEntity> getApplicationEntities() {
        if (applicationEntities == null) {
            applicationEntities = newArrayList();
        }
        return applicationEntities;
    }

    public void setApplicationEntities(
            final List<ApplicationEntity> applicationEntities) {
        this.applicationEntities = applicationEntities;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final VersionEntity that = (VersionEntity) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                          .add("name", name)
                          .toString();
    }
}
