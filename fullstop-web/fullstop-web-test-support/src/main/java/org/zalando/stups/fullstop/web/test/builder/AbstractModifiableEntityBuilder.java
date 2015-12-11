package org.zalando.stups.fullstop.web.test.builder;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static org.joda.time.DateTime.now;

/**
 * @author ahartmann
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
