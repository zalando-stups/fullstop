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
 * @author  mrandi
 */
@Table(name = "violation", schema = "fullstop_data")
@Entity
public class Violation extends AbstractModifiableEntity {

    private String accountId;
    private String region;
    private String message;
    private Object violationObject;
    private String eventId;
    private String comment;
    private Boolean checked;

    private Violation() {
    }

    public Violation(final Object violationObject) {
        this.violationObject = violationObject;
    }

    public Violation(final String accountId, final String region) {
        this.accountId = accountId;
        this.region = region;
    }

    public Violation(final String accountId, final String region, final String message) {
        this.accountId = accountId;
        this.region = region;
        this.message = message;
    }

    public Violation(final String accountId, final String region, final String message, final Object violationObject) {
        this.accountId = accountId;
        this.region = region;
        this.message = message;
        this.violationObject = violationObject;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Object getViolationObject() {
        return violationObject;
    }

    public void setViolationObject(final Object violationObject) {
        this.violationObject = violationObject;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("accountId", accountId)
                .add("region", region)
                .add("message", message)
                .add("violationObject", violationObject)
                .add("eventId", eventId)
                .add("comment", comment)
                .add("checked", checked)
                .toString();
    }
}
