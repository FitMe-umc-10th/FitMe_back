package umc.fitme.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QViewHistory is a Querydsl query type for ViewHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QViewHistory extends EntityPathBase<ViewHistory> {

    private static final long serialVersionUID = -825425456L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QViewHistory viewHistory = new QViewHistory("viewHistory");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final umc.fitme.domain.user.entity.QUser user;

    public final DateTimePath<java.time.LocalDateTime> viewedAt = createDateTime("viewedAt", java.time.LocalDateTime.class);

    public QViewHistory(String variable) {
        this(ViewHistory.class, forVariable(variable), INITS);
    }

    public QViewHistory(Path<? extends ViewHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QViewHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QViewHistory(PathMetadata metadata, PathInits inits) {
        this(ViewHistory.class, metadata, inits);
    }

    public QViewHistory(Class<? extends ViewHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new QPost(forProperty("post")) : null;
        this.user = inits.isInitialized("user") ? new umc.fitme.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

