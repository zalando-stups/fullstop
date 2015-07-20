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
package org.zalando.stups.fullstop.violation;

import static java.lang.String.format;

/**
 * Created by gkneitschel.
 */
public class ViolationBuilder {
    private String eventId;

    private String accountId;

    private String region;

    private String comment;

    private Object violationObject;

    public ViolationBuilder() {
    }

    public ViolationBuilder(final String comment) {
        this.comment = comment;
    }

    public Violation build() {
        DefaultViolation violation = new DefaultViolation();

        violation.setEventId(eventId);
        violation.setAccountId(accountId);
        violation.setRegion(region);
        violation.setComment(comment);
        violation.setMetaInfo(violationObject);

        return violation;
    }

    public ViolationBuilder withEventId(final String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ViolationBuilder withAccountId(final String accoundId) {
        this.accountId = accoundId;
        return this;
    }

    public ViolationBuilder withRegion(final String region) {
        this.region = region;
        return this;
    }

    public ViolationBuilder withComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public ViolationBuilder withComment(final String commentFormat, final Object... args) {
        this.comment = format(commentFormat, args);
        return this;
    }

    public ViolationBuilder withViolationObject(final Object violationObject) {
        this.violationObject = violationObject;
        return this;
    }

}
