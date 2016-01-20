package org.zalando.stups.fullstop.web.test.builder;

import com.google.common.base.Preconditions;
import org.zalando.stups.fullstop.web.test.TestObjectBuilder;
import org.zalando.stups.fullstop.domain.AbstractEntity;

/**
 * @author ahartmann
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
        }
        catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return entity;
    }

}
