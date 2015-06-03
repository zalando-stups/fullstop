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

package org.zalando.stups.fullstop.builder;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.domain.AbstractCreatableEntity;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static org.joda.time.DateTime.now;

/**
 * @author  ahartmann
 */
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractCreatableEntityBuilder<ENTITY_TYPE extends AbstractCreatableEntity, BUILDER_TYPE extends AbstractCreatableEntityBuilder>
    extends AbstractEntityBuilder<ENTITY_TYPE, BUILDER_TYPE> {

    private static final String DEFAULT_CREATED_BY = "unit.test";

    private Optional<Integer> optionalId = absent();
    private Optional<DateTime> optionalCreated = absent();
    private Optional<String> optionalCreatedBy = absent();

    public AbstractCreatableEntityBuilder(final Class<ENTITY_TYPE> entityClass) {
        super(entityClass);
    }

    @Override
    public ENTITY_TYPE build() {
        final ENTITY_TYPE entity = super.build();
        entity.setCreated(optionalCreated.or(now()));
        entity.setCreatedBy(optionalCreatedBy.or(DEFAULT_CREATED_BY));
        entity.setId(optionalId.orNull());
        return entity;
    }

    public BUILDER_TYPE created(final DateTime created) {
        optionalCreated = fromNullable(created);
        return (BUILDER_TYPE) this;
    }

    public BUILDER_TYPE createdBy(final String createdBy) {
        optionalCreatedBy = fromNullable(createdBy);
        return (BUILDER_TYPE) this;
    }

    public BUILDER_TYPE id(final Integer id){
        optionalId = fromNullable(id);
        return (BUILDER_TYPE) this;
    }

}
