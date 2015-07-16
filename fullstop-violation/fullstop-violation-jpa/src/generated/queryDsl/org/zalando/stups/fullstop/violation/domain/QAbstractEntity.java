package org.zalando.stups.fullstop.violation.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QAbstractEntity is a Querydsl query type for AbstractEntity
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QAbstractEntity extends EntityPathBase<AbstractEntity> {

    private static final long serialVersionUID = -654483877L;

    public static final QAbstractEntity abstractEntity = new QAbstractEntity("abstractEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAbstractEntity(String variable) {
        super(AbstractEntity.class, forVariable(variable));
    }

    public QAbstractEntity(Path<? extends AbstractEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAbstractEntity(PathMetadata<?> metadata) {
        super(AbstractEntity.class, metadata);
    }

}

