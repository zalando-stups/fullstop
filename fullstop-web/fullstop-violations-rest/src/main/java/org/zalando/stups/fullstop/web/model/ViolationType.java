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
package org.zalando.stups.fullstop.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import static com.google.common.base.MoreObjects.toStringHelper;

@ApiModel(description = "")
public class ViolationType {

    private String id = null;

    private String helpText = null;

    private Integer violationSeverity = null;

    private boolean isAuditRelevant;

    private Long version = null;

    private DateTime created = null;

    private String createdBy = null;

    private DateTime lastModified = null;

    private String lastModifiedBy = null;

    @ApiModelProperty(value = "")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    @ApiModelProperty(value = "")
    public Integer getViolationSeverity() {
        return violationSeverity;
    }

    public void setViolationSeverity(Integer violationSeverity) {
        this.violationSeverity = violationSeverity;
    }

    @ApiModelProperty(value = "")
    public boolean isAuditRelevant() {
        return isAuditRelevant;
    }

    public void setIsAuditRelevant(boolean isAuditRelevant) {
        this.isAuditRelevant = isAuditRelevant;
    }

    @ApiModelProperty(value = "")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    @Override public String toString() {
        return toStringHelper(this)
                .add("lastModifiedBy", lastModifiedBy)
                .add("lastModified", lastModified)
                .add("createdBy", createdBy)
                .add("created", created)
                .add("version", version)
                .add("isAuditRelevant", isAuditRelevant)
                .add("violationSeverity", violationSeverity)
                .add("helpText", helpText)
                .add("id", id)
                .toString();
    }
}
