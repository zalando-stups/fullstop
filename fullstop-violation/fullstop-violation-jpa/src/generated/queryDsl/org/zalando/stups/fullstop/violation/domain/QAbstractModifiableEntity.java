package org.zalando.stups.fullstop.violation.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAbstractModifiableEntity is a Querydsl query type for AbstractModifiableEntity
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QAbstractModifiableEntity extends EntityPathBase<AbstractModifiableEntity> {

    private static final long serialVersionUID = 1981049471L;

    public static final QAbstractModifiableEntity abstractModifiableEntity = new QAbstractModifiableEntity("abstractModifiableEntity");

    public final QAbstractCreatableEntity _super = new QAbstractCreatableEntity(this);

    //inherited
    public final DateTimePath<org.joda.time.DateTime> created = _super.created;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DateTimePath<org.joda.time.DateTime> lastModified = createDateTime("lastModified", org.joda.time.DateTime.class);

    public final StringPath lastModifiedBy = createString("lastModifiedBy");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QAbstractModifiableEntity(String variable) {
        super(AbstractModifiableEntity.class, forVariable(variable));
    }

    public QAbstractModifiableEntity(Path<? extends AbstractModifiableEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAbstractModifiableEntity(PathMetadata<?> metadata) {
        super(AbstractModifiableEntity.class, metadata);
    }

}

