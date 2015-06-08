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
package org.zalando.stups.fullstop.builder.domain;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.UUID;

import org.zalando.stups.fullstop.builder.AbstractModifiableEntityBuilder;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

/**
 * Created by mrandi. Builder example
 */
public class ViolationBuilder extends AbstractModifiableEntityBuilder<ViolationEntity, ViolationBuilder> {

    private String eventId;
    private String accountId;
    private String region;
    private String message;
    private Object violationObject;
    private String comment;
    private Boolean checked;

    public ViolationBuilder() {
        super(ViolationEntity.class);
    }

    public static ViolationBuilder violation() {
        return new ViolationBuilder();
    }

    @Override
    public ViolationEntity build() {
        final ViolationEntity entity = super.build();

        entity.setEventId(firstNonNull(eventId, UUID.randomUUID().toString()));
        entity.setAccountId(firstNonNull(accountId, "my account id" + Math.random()));
        entity.setRegion(firstNonNull(region, "my region" + Math.random()));
        entity.setMessage(firstNonNull(message, "my message" + Math.random()));
        entity.setViolationObject(firstNonNull(violationObject, new Object()));
        entity.setComment(firstNonNull(comment, "my comment" + Math.random()));
        entity.setChecked(firstNonNull(checked, false));

        return entity;
    }

    public ViolationBuilder eventId(final String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ViolationBuilder accountId(final String accountId) {
        this.accountId = accountId;
        return this;
    }

    public ViolationBuilder region(final String region) {
        this.region = region;
        return this;
    }

    public ViolationBuilder message(final String message) {
        this.message = message;
        return this;
    }

    public ViolationBuilder violationObject(final Object violationObject) {
        this.violationObject = violationObject;
        return this;
    }

    public ViolationBuilder comment(final String comment) {
        this.comment = comment;
        return this;
    }

    public ViolationBuilder checked(final Boolean checked) {
        this.checked = checked;
        return this;
    }
}
