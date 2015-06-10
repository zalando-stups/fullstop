/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.swagger.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(description = "")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Violation {

    private Long id = null;

    private Long version = null;

    private String eventId = null;

    private String accountId = null;

    private String region = null;

    private String message = null;

    private Object violationObject = null;

    private String comment = null;

    private Date created = null;

    private String createdBy = null;

    private Date lastModified = null;

    private String lastModifiedBy = null;

    @ApiModelProperty(value = "")
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("version")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("accountId")
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("region")
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("violationObject")
    public Object getViolationObject() {
        return violationObject;
    }

    public void setViolationObject(Object violationObject) {
        this.violationObject = violationObject;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("created_by")
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("last_modified_by")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Violation {\n");

        sb.append("  id: ").append(id).append("\n");
        sb.append("  version: ").append(version).append("\n");
        sb.append("  eventId: ").append(eventId).append("\n");
        sb.append("  accountId: ").append(accountId).append("\n");
        sb.append("  region: ").append(region).append("\n");
        sb.append("  message: ").append(message).append("\n");
        sb.append("  violationObject: ").append(violationObject).append("\n");
        sb.append("  comment: ").append(comment).append("\n");
        sb.append("  created: ").append(created).append("\n");
        sb.append("  createdBy: ").append(createdBy).append("\n");
        sb.append("  lastModified: ").append(lastModified).append("\n");
        sb.append("  lastModifiedBy: ").append(lastModifiedBy).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
