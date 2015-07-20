/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.*;
import java.util.List;

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
    @JoinTable(schema = "fullstop_data", name = "app_has_version",
            joinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "app_version_id", referencedColumnName = "id"))
    private List<VersionEntity> versionEntities;

    public ApplicationEntity() {
    }

    public ApplicationEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VersionEntity> getVersionEntities() {

        if (versionEntities == null) {
            versionEntities = newArrayList();
        }

        return versionEntities;
    }

    public void setVersionEntities(
            List<VersionEntity> versionEntities) {
        this.versionEntities = versionEntities;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ApplicationEntity that = (ApplicationEntity) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return !(versionEntities != null ?
                !versionEntities.equals(that.versionEntities) :
                that.versionEntities != null);

    }

    @Override public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (versionEntities != null ? versionEntities.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("name", name)
                .toString();
    }
}
