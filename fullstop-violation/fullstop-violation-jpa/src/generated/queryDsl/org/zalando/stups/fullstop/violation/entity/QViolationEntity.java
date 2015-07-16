package org.zalando.stups.fullstop.violation.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QViolationEntity is a Querydsl query type for ViolationEntity
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QViolationEntity extends EntityPathBase<ViolationEntity> {

    private static final long serialVersionUID = -462116463L;

    public static final QViolationEntity violationEntity = new QViolationEntity("violationEntity");

    public final org.zalando.stups.fullstop.violation.domain.QAbstractModifiableEntity _super = new org.zalando.stups.fullstop.violation.domain.QAbstractModifiableEntity(this);

    public final StringPath accountId = createString("accountId");

    public final StringPath comment = createString("comment");

    //inherited
    public final DateTimePath<org.joda.time.DateTime> created = _super.created;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final StringPath eventId = createString("eventId");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<org.joda.time.DateTime> lastModified = _super.lastModified;

    //inherited
    public final StringPath lastModifiedBy = _super.lastModifiedBy;

    public final StringPath message = createString("message");

    public final StringPath region = createString("region");

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final SimplePath<Object> violationObject = createSimple("violationObject", Object.class);

    public QViolationEntity(String variable) {
        super(ViolationEntity.class, forVariable(variable));
    }

    public QViolationEntity(Path<? extends ViolationEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QViolationEntity(PathMetadata<?> metadata) {
        super(ViolationEntity.class, metadata);
    }

}

