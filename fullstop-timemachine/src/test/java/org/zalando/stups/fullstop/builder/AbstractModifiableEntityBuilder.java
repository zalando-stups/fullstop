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
package org.zalando.stups.fullstop.builder;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static org.joda.time.DateTime.now;

/**
 * @author  ahartmann
 */
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractModifiableEntityBuilder<ENTITY_TYPE extends AbstractModifiableEntity, BUILDER_TYPE extends AbstractModifiableEntityBuilder>
    extends AbstractCreatableEntityBuilder<ENTITY_TYPE, BUILDER_TYPE> {

    private static final String DEFAULT_MODIFIER = "unit.test";

    private Optional<DateTime> optionalLastModified = absent();
    private Optional<String> optionalLastModifiedBy = absent();
    private Optional<Long> optionalVersion = absent();

    public AbstractModifiableEntityBuilder(final Class<ENTITY_TYPE> entityClass) {
        super(entityClass);
    }

    @Override
    public ENTITY_TYPE build() {
        final ENTITY_TYPE entity = super.build();
        entity.setLastModified(optionalLastModified.or(now()));
        entity.setLastModifiedBy(optionalLastModifiedBy.or(DEFAULT_MODIFIER));
        entity.setVersion(optionalVersion.orNull());
        return entity;
    }

    public BUILDER_TYPE lastModified(final DateTime lastModified) {
        optionalLastModified = fromNullable(lastModified);
        return (BUILDER_TYPE) this;
    }

    public BUILDER_TYPE lastModifiedBy(final String lastModifiedBy) {
        optionalLastModifiedBy = fromNullable(lastModifiedBy);
        return (BUILDER_TYPE) this;
    }

    public BUILDER_TYPE version(final Long version) {
        optionalVersion = fromNullable(version);
        return (BUILDER_TYPE) this;
    }
}
