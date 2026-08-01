package umc.fitme.domain.user.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserApplication is a Querydsl query type for UserApplication
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserApplication extends EntityPathBase<UserApplication> {

    private static final long serialVersionUID = 826028283L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserApplication userApplication = new QUserApplication("userApplication");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isApplied = createBoolean("isApplied");

    public final StringPath memo = createString("memo");

    public final umc.fitme.domain.post.entity.QPost post;

    public final EnumPath<umc.fitme.domain.user.enums.Status> status = createEnum("status", umc.fitme.domain.user.enums.Status.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final umc.fitme.domain.user.entity.QUser user;

    public QUserApplication(String variable) {
        this(UserApplication.class, forVariable(variable), INITS);
    }

    public QUserApplication(Path<? extends UserApplication> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserApplication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserApplication(PathMetadata metadata, PathInits inits) {
        this(UserApplication.class, metadata, inits);
    }

    public QUserApplication(Class<? extends UserApplication> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new umc.fitme.domain.post.entity.QPost(forProperty("post")) : null;
        this.user = inits.isInitialized("user") ? new umc.fitme.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

