package umc.fitme.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSearchRecent is a Querydsl query type for SearchRecent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchRecent extends EntityPathBase<SearchRecent> {

    private static final long serialVersionUID = 279718477L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSearchRecent searchRecent = new QSearchRecent("searchRecent");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final DateTimePath<java.time.LocalDateTime> updateAt = createDateTime("updateAt", java.time.LocalDateTime.class);

    public final QUser user;

    public QSearchRecent(String variable) {
        this(SearchRecent.class, forVariable(variable), INITS);
    }

    public QSearchRecent(Path<? extends SearchRecent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSearchRecent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSearchRecent(PathMetadata metadata, PathInits inits) {
        this(SearchRecent.class, metadata, inits);
    }

    public QSearchRecent(Class<? extends SearchRecent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

