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
package org.zalando.stups.fullstop.violation.domain;

import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.zalando.stups.fullstop.violation.domain.validation.groups.PersistenceOnly;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

/**
 * @author  ahartmann
 */
@MappedSuperclass
public abstract class AbstractModifiableEntity extends AbstractCreatableEntity {

    /**
     * Enables optimistic locking.
     */
    @Version
    private Long version;

    @LastModifiedDate
    @NotNull(groups = {PersistenceOnly.class})
    @Column(nullable = false)
    private DateTime lastModified;

    @LastModifiedBy
    @NotEmpty(groups = {PersistenceOnly.class})
    @Column(nullable = false)
    protected String lastModifiedBy;

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
    protected void addToStringFields(final Objects.ToStringHelper helper) {
        super.addToStringFields(helper);

        helper.add("lastModified", lastModified);
        helper.add("lastModifiedBy", lastModifiedBy);
        helper.add("version", version);
    }
}
