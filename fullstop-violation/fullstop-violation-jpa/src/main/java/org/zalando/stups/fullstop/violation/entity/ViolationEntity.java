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

import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.*;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author mrandi
 */
@Table(name = "violation", schema = "fullstop_data", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id","account_id","region"}))//,"violation_type_entity_id"
@Entity
public class ViolationEntity extends AbstractModifiableEntity {

    private String eventId;

    private String accountId;

    private String region;

    private Object metaInfo;

    private String comment;

    private String pluginFullQualifiedClassName;

    @ManyToOne
    private ViolationTypeEntity violationTypeEntity;

    public ViolationEntity(String eventId, String accountId, String region, Object metaInfo,
            String comment) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.region = region;
        this.metaInfo = metaInfo;
        this.comment = comment;
    }

    public ViolationEntity() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public Object getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(final Object metaInfo) {
        this.metaInfo = metaInfo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPluginFullQualifiedClassName() {
        return pluginFullQualifiedClassName;
    }

    public void setPluginFullQualifiedClassName(String pluginFullQualifiedClassName) {
        this.pluginFullQualifiedClassName = pluginFullQualifiedClassName;
    }

    public ViolationTypeEntity getViolationTypeEntity() {
        return violationTypeEntity;
    }

    public void setViolationTypeEntity(
            ViolationTypeEntity violationTypeEntity) {
        this.violationTypeEntity = violationTypeEntity;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("eventId", eventId)
                .add("accountId", accountId)
                .add("region", region)
                .add("metaInfo", metaInfo)
                .add("comment", comment)
                .add("pluginFullQualifiedClassName", pluginFullQualifiedClassName)
                .add("violationTypeEntity", violationTypeEntity)
                .toString();
    }
}
