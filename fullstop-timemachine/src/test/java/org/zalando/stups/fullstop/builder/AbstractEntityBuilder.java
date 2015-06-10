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

import com.google.common.base.Preconditions;
import org.zalando.stups.fullstop.common.test.support.TestObjectBuilder;
import org.zalando.stups.fullstop.violation.domain.AbstractEntity;

/**
 * @author  ahartmann
 */
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractEntityBuilder<ENTITY_TYPE extends AbstractEntity, BUILDER_TYPE extends AbstractEntityBuilder>
    implements TestObjectBuilder<ENTITY_TYPE> {

    private final Class<ENTITY_TYPE> entityClass;

    public AbstractEntityBuilder(final Class<ENTITY_TYPE> entityClass) {
        this.entityClass = Preconditions.checkNotNull(entityClass, "Entity class must not be null");
    }

    @Override
    public ENTITY_TYPE build() {
        final ENTITY_TYPE entity;

        try {
            entity = entityClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return entity;
    }

}
