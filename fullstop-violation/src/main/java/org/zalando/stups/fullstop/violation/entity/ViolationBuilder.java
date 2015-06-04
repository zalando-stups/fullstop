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

/**
 * Created by gkneitschel.
 */
public class ViolationBuilder {
    private String eventId;
    private String accountId;
    private String region;
    private String message;
    private Object violationObject;
    private String comment;
    private Boolean checked;

    public ViolationBuilder(final String message) {
        this.message = message;
    }

    public Violation build() {
        Violation violation = new Violation();

        violation.setEventId(eventId);
        violation.setAccountId(accountId);
        violation.setRegion(region);
        violation.setMessage(message);
        violation.setViolationObject(violationObject);
        violation.setComment(comment);
        violation.setChecked(checked);

        return violation;
    }

    public ViolationBuilder withEventId(final String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ViolationBuilder withAccoundId(final String accoundId) {
        this.accountId = accoundId;
        return this;
    }

    public ViolationBuilder withRegion(final String region) {
        this.region = region;
        return this;
    }

    public ViolationBuilder withMessage(final String message) {
        this.message = message;
        return this;
    }

    public ViolationBuilder withViolationObject(final Object violationObject) {
        this.violationObject = violationObject;
        return this;
    }

    public ViolationBuilder withComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public ViolationBuilder isChecked(final Boolean checked) {
        this.checked = checked;
        return this;
    }

}
