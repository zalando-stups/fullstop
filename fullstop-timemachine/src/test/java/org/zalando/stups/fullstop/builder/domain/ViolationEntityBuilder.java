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
package org.zalando.stups.fullstop.builder.domain;

import org.zalando.stups.fullstop.builder.AbstractModifiableEntityBuilder;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.UUID;

/**
 * Created by mrandi. Builder example
 */
public class ViolationEntityBuilder extends AbstractModifiableEntityBuilder<ViolationEntity, ViolationEntityBuilder> {

    private String eventId = UUID.randomUUID().toString();

    private String accountId = "my account id" + Math.random();

    private String region = "my region" + Math.random();

    private String message = "my message" + Math.random();

    private Object violationObject;

    private String comment = "my comment" + Math.random();

    public ViolationEntityBuilder() {
        super(ViolationEntity.class);
    }

    public static ViolationEntityBuilder violation() {
        return new ViolationEntityBuilder();
    }

    @Override
    public ViolationEntity build() {
        final ViolationEntity entity = super.build();

        entity.setEventId(eventId);
        entity.setAccountId(accountId);
        entity.setRegion(region);
        entity.setMessage(message);
        entity.setViolationObject(violationObject);
        entity.setComment(comment);

        return entity;
    }

    public ViolationEntityBuilder eventId(final String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ViolationEntityBuilder accountId(final String accountId) {
        this.accountId = accountId;
        return this;
    }

    public ViolationEntityBuilder region(final String region) {
        this.region = region;
        return this;
    }

    public ViolationEntityBuilder message(final String message) {
        this.message = message;
        return this;
    }

    public ViolationEntityBuilder violationObject(final Object violationObject) {
        this.violationObject = violationObject;
        return this;
    }

    public ViolationEntityBuilder comment(final String comment) {
        this.comment = comment;
        return this;
    }
}
