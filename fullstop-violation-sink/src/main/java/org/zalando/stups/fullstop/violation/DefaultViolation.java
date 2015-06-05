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
package org.zalando.stups.fullstop.violation;

import com.google.common.base.MoreObjects;

/**
 * @author  jbellmann
 */
class DefaultViolation implements Violation {

    private String eventId;
    private String accountId;
    private String region;
    private String message;
    private Object violationObject;
    private String comment;
    private Boolean checked;

    DefaultViolation(final String eventId, final String accountId, final String region, final String message,
            final Object violationObject, final String comment, final Boolean checked) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.region = region;
        this.message = message;
        this.violationObject = violationObject;
        this.comment = comment;
        this.checked = checked;
    }

    DefaultViolation() { }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(final String eventId) {
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

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(final Boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("accountId", accountId).add("region", region)
                          .add("message", message).add("violationObject", violationObject).add("eventId", eventId)
                          .add("comment", comment).add("checked", checked).toString();
    }

}
