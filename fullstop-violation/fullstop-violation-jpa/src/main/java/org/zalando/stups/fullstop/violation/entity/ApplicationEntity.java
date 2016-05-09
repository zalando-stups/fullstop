package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by gkneitschel.
 */

@Table(name = "application", schema = "fullstop_data")
@Entity
public class ApplicationEntity extends AbstractModifiableEntity {

    @Column(unique = true)
    private String name;

    @ManyToMany
    private List<VersionEntity> versionEntities;

    public ApplicationEntity() {
    }

    public ApplicationEntity(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<VersionEntity> getVersionEntities() {

        if (versionEntities == null) {
            versionEntities = newArrayList();
        }

        return versionEntities;
    }

    public void setVersionEntities(
            final List<VersionEntity> versionEntities) {
        this.versionEntities = versionEntities;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ApplicationEntity that = (ApplicationEntity) o;
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
