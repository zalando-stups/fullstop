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

import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by gkneitschel.
 */
@Table(name = "lifecycle", schema = "fullstop_data")
@Entity
public class LifecycleEntity extends AbstractModifiableEntity {
    private DateTime eventDate;

    private String region;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application", referencedColumnName = "id")
    private ApplicationEntity applicationEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_version", referencedColumnName = "id")
    private VersionEntity versionEntity;

    private String eventType;

    private String userdataPath;

    private DateTime instanceBootTime;

    private String instanceId;

    public DateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(DateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public ApplicationEntity getApplicationEntity() {
        return applicationEntity;
    }

    public void setApplicationEntity(ApplicationEntity applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    public VersionEntity getVersionEntity() {
        return versionEntity;
    }

    public void setVersionEntity(VersionEntity versionEntity) {
        this.versionEntity = versionEntity;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserdataPath() {
        return userdataPath;
    }

    public void setUserdataPath(String userdataPath) {
        this.userdataPath = userdataPath;
    }

    public DateTime getInstanceBootTime() {
        return instanceBootTime;
    }

    public void setInstanceBootTime(DateTime instanceBootTime) {
        this.instanceBootTime = instanceBootTime;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("eventDate", eventDate)
                .add("region", region)
                .add("applicationEntity", applicationEntity)
                .add("versionEntity", versionEntity)
                .add("eventType", eventType)
                .add("userdataPath", userdataPath)
                .add("instanceBootTime", instanceBootTime)
                .add("instanceId", instanceId)
                .toString();
    }
}