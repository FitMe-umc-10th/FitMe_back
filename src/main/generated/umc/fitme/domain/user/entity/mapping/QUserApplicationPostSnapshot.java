package umc.fitme.domain.user.entity.mapping;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserApplicationPostSnapshot is a Querydsl query type for UserApplicationPostSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserApplicationPostSnapshot extends EntityPathBase<UserApplicationPostSnapshot> {

    private static final long serialVersionUID = -1704061153L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserApplicationPostSnapshot userApplicationPostSnapshot = new QUserApplicationPostSnapshot("userApplicationPostSnapshot");

    public final StringPath applicationMethod = createString("applicationMethod");

    public final StringPath applicationUrl = createString("applicationUrl");

    public final DatePath<java.time.LocalDate> applyEndAt = createDate("applyEndAt", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> applyStartAt = createDate("applyStartAt", java.time.LocalDate.class);

    public final StringPath gradeRequirement = createString("gradeRequirement");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath incomeRequirement = createString("incomeRequirement");

    public final StringPath organizer = createString("organizer");

    public final NumberPath<Long> originalPostId = createNumber("originalPostId", Long.class);

    public final StringPath participantLimit = createString("participantLimit");

    public final StringPath posterImageUrl = createString("posterImageUrl");

    public final EnumPath<umc.fitme.domain.post.enums.PostType> postType = createEnum("postType", umc.fitme.domain.post.enums.PostType.class);

    public final StringPath regionRequirement = createString("regionRequirement");

    public final StringPath rewardTotal = createString("rewardTotal");

    public final StringPath summary = createString("summary");

    public final StringPath supportAmount = createString("supportAmount");

    public final NumberPath<Long> supportAmountValue = createNumber("supportAmountValue", Long.class);

    public final StringPath target = createString("target");

    public final StringPath title = createString("title");

    public final QUserApplication userApplication;

    public QUserApplicationPostSnapshot(String variable) {
        this(UserApplicationPostSnapshot.class, forVariable(variable), INITS);
    }

    public QUserApplicationPostSnapshot(Path<? extends UserApplicationPostSnapshot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserApplicationPostSnapshot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserApplicationPostSnapshot(PathMetadata metadata, PathInits inits) {
        this(UserApplicationPostSnapshot.class, metadata, inits);
    }

    public QUserApplicationPostSnapshot(Class<? extends UserApplicationPostSnapshot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userApplication = inits.isInitialized("userApplication") ? new QUserApplication(forProperty("userApplication"), inits.get("userApplication")) : null;
    }

}

