package umc.fitme.domain.user.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSave is a Querydsl query type for UserSave
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSave extends EntityPathBase<UserSave> {

    private static final long serialVersionUID = 284069938L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSave userSave = new QUserSave("userSave");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isSaved = createBoolean("isSaved");

    public final umc.fitme.domain.post.entity.QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final umc.fitme.domain.user.entity.QUser user;

    public QUserSave(String variable) {
        this(UserSave.class, forVariable(variable), INITS);
    }

    public QUserSave(Path<? extends UserSave> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSave(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSave(PathMetadata metadata, PathInits inits) {
        this(UserSave.class, metadata, inits);
    }

    public QUserSave(Class<? extends UserSave> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new umc.fitme.domain.post.entity.QPost(forProperty("post")) : null;
        this.user = inits.isInitialized("user") ? new umc.fitme.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

