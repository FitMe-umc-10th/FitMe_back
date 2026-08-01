package umc.fitme.domain.interest.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostInterest is a Querydsl query type for PostInterest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostInterest extends EntityPathBase<PostInterest> {

    private static final long serialVersionUID = 331616531L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostInterest postInterest = new QPostInterest("postInterest");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final umc.fitme.domain.interest.entity.QInterest interest;

    public final umc.fitme.domain.post.entity.QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPostInterest(String variable) {
        this(PostInterest.class, forVariable(variable), INITS);
    }

    public QPostInterest(Path<? extends PostInterest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostInterest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostInterest(PathMetadata metadata, PathInits inits) {
        this(PostInterest.class, metadata, inits);
    }

    public QPostInterest(Class<? extends PostInterest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.interest = inits.isInitialized("interest") ? new umc.fitme.domain.interest.entity.QInterest(forProperty("interest")) : null;
        this.post = inits.isInitialized("post") ? new umc.fitme.domain.post.entity.QPost(forProperty("post")) : null;
    }

}

