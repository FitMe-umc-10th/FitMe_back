package umc.fitme.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QContest is a Querydsl query type for Contest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContest extends EntityPathBase<Contest> {

    private static final long serialVersionUID = -538230475L;

    public static final QContest contest = new QContest("contest");

    public final QPost _super = new QPost(this);

    //inherited
    public final StringPath applicationMethod = _super.applicationMethod;

    //inherited
    public final StringPath applicationUrl = _super.applicationUrl;

    //inherited
    public final DatePath<java.time.LocalDate> applyEndAt = _super.applyEndAt;

    //inherited
    public final DatePath<java.time.LocalDate> applyStartAt = _super.applyStartAt;

    public final EnumPath<umc.fitme.domain.post.enums.ContestCategory> contestCategory = createEnum("contestCategory", umc.fitme.domain.post.enums.ContestCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath imageUrl = _super.imageUrl;

    //inherited
    public final StringPath organizer = _super.organizer;

    public final StringPath participantLimit = createString("participantLimit");

    public final StringPath posterImageUrl = createString("posterImageUrl");

    //inherited
    public final NumberPath<Integer> postRank = _super.postRank;

    //inherited
    public final EnumPath<umc.fitme.domain.post.enums.PostType> postType = _super.postType;

    public final StringPath rewardTotal = createString("rewardTotal");

    //inherited
    public final NumberPath<Integer> savedCount = _super.savedCount;

    //inherited
    public final StringPath summary = _super.summary;

    public final StringPath target = createString("target");

    //inherited
    public final StringPath title = _super.title;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Integer> viewCount = _super.viewCount;

    public QContest(String variable) {
        super(Contest.class, forVariable(variable));
    }

    public QContest(Path<? extends Contest> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContest(PathMetadata metadata) {
        super(Contest.class, metadata);
    }

}

