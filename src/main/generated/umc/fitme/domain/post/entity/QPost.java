package umc.fitme.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = -1094457313L;

    public static final QPost post = new QPost("post");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    public final BooleanPath active = createBoolean("active");

    public final StringPath applicationMethod = createString("applicationMethod");

    public final StringPath applicationUrl = createString("applicationUrl");

    public final DatePath<java.time.LocalDate> applyEndAt = createDate("applyEndAt", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> applyStartAt = createDate("applyStartAt", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath organizer = createString("organizer");

    public final NumberPath<Integer> postRank = createNumber("postRank", Integer.class);

    public final EnumPath<umc.fitme.domain.post.enums.PostType> postType = createEnum("postType", umc.fitme.domain.post.enums.PostType.class);

    public final NumberPath<Integer> savedCount = createNumber("savedCount", Integer.class);

    public final StringPath summary = createString("summary");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QPost(String variable) {
        super(Post.class, forVariable(variable));
    }

    public QPost(Path<? extends Post> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPost(PathMetadata metadata) {
        super(Post.class, metadata);
    }

}

