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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collection;

/**
 * Created by gkneitschel.
 */
@Table(name = "app_version", schema = "fullstop_data")
@Entity
public class ApplicationVersionEntity extends AbstractModifiableEntity {
    private String applicationVersion;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "app_has_version",
            joinColumns = @JoinColumn(name = "app_version_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id"))
    private Collection<ApplicationVersionEntity> application;

    public ApplicationVersionEntity(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public Collection<ApplicationVersionEntity> getApplication() {
        return application;
    }

    public void setApplication(
            Collection<ApplicationVersionEntity> application) {
        this.application = application;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("applicationVersion", applicationVersion)
                .toString();
    }
}
