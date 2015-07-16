package org.zalando.stups.fullstop.violation.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAbstractCreatableEntity is a Querydsl query type for AbstractCreatableEntity
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QAbstractCreatableEntity extends EntityPathBase<AbstractCreatableEntity> {

    private static final long serialVersionUID = 1727291310L;

    public static final QAbstractCreatableEntity abstractCreatableEntity = new QAbstractCreatableEntity("abstractCreatableEntity");

    public final QAbstractEntity _super = new QAbstractEntity(this);

    public final DateTimePath<org.joda.time.DateTime> created = createDateTime("created", org.joda.time.DateTime.class);

    public final StringPath createdBy = createString("createdBy");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public QAbstractCreatableEntity(String variable) {
        super(AbstractCreatableEntity.class, forVariable(variable));
    }

    public QAbstractCreatableEntity(Path<? extends AbstractCreatableEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAbstractCreatableEntity(PathMetadata<?> metadata) {
        super(AbstractCreatableEntity.class, metadata);
    }

}

