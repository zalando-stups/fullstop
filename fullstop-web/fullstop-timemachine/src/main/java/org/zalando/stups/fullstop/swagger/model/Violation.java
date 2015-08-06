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
package org.zalando.stups.fullstop.swagger.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import static com.google.common.base.MoreObjects.toStringHelper;

@ApiModel(description = "")
public class Violation {

    private Long id = null;

    private Long version = null;

    private String eventId = null;

    private String accountId = null;

    private String region = null;

    private Object metaInfo = null;

    private String comment = null;

    private DateTime created = null;

    private String createdBy = null;

    private DateTime lastModified = null;

    private String lastModifiedBy = null;

    private String pluginFullyQualifiedClassName = null;

    private ViolationTypeEntity violationTypeEntity = null;

    private String instanceId = null;

    @ApiModelProperty(value = "")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @ApiModelProperty(value = "")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @ApiModelProperty(value = "")
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @ApiModelProperty(value = "")
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @ApiModelProperty(value = "")
    public Object getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(Object metaInfo) {
        this.metaInfo = metaInfo;
    }

    @ApiModelProperty(value = "")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @ApiModelProperty(value = "")
    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    @ApiModelProperty(value = "")
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @ApiModelProperty(value = "")
    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    @ApiModelProperty(value = "")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @ApiModelProperty(value = "")
    public String getPluginFullyQualifiedClassName() {
        return pluginFullyQualifiedClassName;
    }

    public void setPluginFullyQualifiedClassName(String pluginFullyQualifiedClassName) {
        this.pluginFullyQualifiedClassName = pluginFullyQualifiedClassName;
    }

    @ApiModelProperty(value = "")
    public ViolationTypeEntity getViolationTypeEntity() {
        return violationTypeEntity;
    }

    public void setViolationTypeEntity(ViolationTypeEntity violationTypeEntity) {
        this.violationTypeEntity = violationTypeEntity;
    }

    @ApiModelProperty(value = "")
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("version", version)
                .add("eventId", eventId)
                .add("accountId", accountId)
                .add("region", region)
                .add("metaInfo", metaInfo)
                .add("comment", comment)
                .add("created", created)
                .add("createdBy", createdBy)
                .add("lastModified", lastModified)
                .add("lastModifiedBy", lastModifiedBy)
                .add("pluginFullyQualifiedClassName", pluginFullyQualifiedClassName)
                .add("violationTypeEntity", violationTypeEntity)
                .add("instanceId", instanceId)
                .toString();
    }
}
