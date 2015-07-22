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

/**
 * Created by gkneitschel.
 */
public class ViolationBuilder {
    private String eventId;

    private String accountId;

    private String region;

    private Object metaInfo;

    private String type;

    public ViolationBuilder() {
    }

    public Violation build() {
        DefaultViolation violation = new DefaultViolation();

        violation.setEventId(eventId);
        violation.setAccountId(accountId);
        violation.setRegion(region);
        violation.setMetaInfo(metaInfo);
        violation.setViolationType(type);

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

    public ViolationBuilder withMetaInfo(final Object metaInfo) {
        this.metaInfo = metaInfo;
        return this;
    }

    public ViolationBuilder withType(String type) {
        this.type = type;
        return this;
    }
}
