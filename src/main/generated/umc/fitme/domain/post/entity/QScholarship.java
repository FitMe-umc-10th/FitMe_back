package umc.fitme.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScholarship is a Querydsl query type for Scholarship
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScholarship extends EntityPathBase<Scholarship> {

    private static final long serialVersionUID = 919933379L;

    public static final QScholarship scholarship = new QScholarship("scholarship");

    public final QPost _super = new QPost(this);

    //inherited
    public final StringPath applicationMethod = _super.applicationMethod;

    //inherited
    public final StringPath applicationUrl = _super.applicationUrl;

    //inherited
    public final DatePath<java.time.LocalDate> applyEndAt = _super.applyEndAt;

    //inherited
    public final DatePath<java.time.LocalDate> applyStartAt = _super.applyStartAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath gradeRequirement = createString("gradeRequirement");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath imageUrl = _super.imageUrl;

    public final StringPath incomeRequirement = createString("incomeRequirement");

    //inherited
    public final StringPath organizer = _super.organizer;

    //inherited
    public final NumberPath<Integer> postRank = _super.postRank;

    //inherited
    public final EnumPath<umc.fitme.domain.post.enums.PostType> postType = _super.postType;

    public final StringPath regionRequirement = createString("regionRequirement");

    //inherited
    public final NumberPath<Integer> savedCount = _super.savedCount;

    //inherited
    public final StringPath summary = _super.summary;

    public final StringPath supportAmount = createString("supportAmount");

    public final NumberPath<Long> supportAmountValue = createNumber("supportAmountValue", Long.class);

    //inherited
    public final StringPath title = _super.title;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Integer> viewCount = _super.viewCount;

    public QScholarship(String variable) {
        super(Scholarship.class, forVariable(variable));
    }

    public QScholarship(Path<? extends Scholarship> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScholarship(PathMetadata metadata) {
        super(Scholarship.class, metadata);
    }

}

