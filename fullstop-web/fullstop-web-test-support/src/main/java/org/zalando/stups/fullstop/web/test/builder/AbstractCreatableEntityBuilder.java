package org.zalando.stups.fullstop.web.test.builder;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.domain.AbstractCreatableEntity;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static org.joda.time.DateTime.now;

/**
 * @author ahartmann
 */
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractCreatableEntityBuilder<ENTITY_TYPE extends AbstractCreatableEntity, BUILDER_TYPE extends AbstractCreatableEntityBuilder>
        extends AbstractEntityBuilder<ENTITY_TYPE, BUILDER_TYPE> {

    private static final String DEFAULT_CREATED_BY = "unit.test";

    private Optional<Long> optionalId = absent();

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

    public BUILDER_TYPE id(final Long id) {
        optionalId = fromNullable(id);
        return (BUILDER_TYPE) this;
    }

}
