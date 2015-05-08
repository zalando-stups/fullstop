/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by gkneitschel.
 */
@Table(name = "fullstop", schema = "fullstop_violations")
@Entity
public class ViolationEntity extends AbstractModifiableEntity {

    private String accountId;
    private String region;
    private String message;
    private String violationObject;
    private String eventId;
    private String comment;
    private Boolean checked;

    protected ViolationEntity() {
    }

    public ViolationEntity(final String accountId, final String region, final String message, final String violationObject) {

        this.accountId = accountId;
        this.region = region;
        this.message = message;
        this.violationObject = violationObject;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setViolationObject(String violationObject) {
        this.violationObject = violationObject;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getEventId() {
        return eventId;
    }

    public String getComment() {
        return comment;
    }

    public Boolean getChecked() {
        return checked;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getMessage() {
        return message;
    }

    public String getRegion() {
        return region;
    }

    public String getViolationObject() {
        return violationObject;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("accountId", accountId).add("region", region)
                .add("message", message).add("violationObject", violationObject).toString();
    }


}
