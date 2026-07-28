package umc.fitme.domain.post.sync.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScholarshipSyncLog is a Querydsl query type for ScholarshipSyncLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScholarshipSyncLog extends EntityPathBase<ScholarshipSyncLog> {

    private static final long serialVersionUID = 1273978859L;

    public static final QScholarshipSyncLog scholarshipSyncLog = new QScholarshipSyncLog("scholarshipSyncLog");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> inactivatedCount = createNumber("inactivatedCount", Integer.class);

    public final NumberPath<Integer> insertedCount = createNumber("insertedCount", Integer.class);

    public final EnumPath<umc.fitme.domain.post.sync.enums.ScholarshipSyncStatus> status = createEnum("status", umc.fitme.domain.post.sync.enums.ScholarshipSyncStatus.class);

    public final DateTimePath<java.time.LocalDateTime> syncedAt = createDateTime("syncedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> totalCount = createNumber("totalCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> updatedCount = createNumber("updatedCount", Integer.class);

    public QScholarshipSyncLog(String variable) {
        super(ScholarshipSyncLog.class, forVariable(variable));
    }

    public QScholarshipSyncLog(Path<? extends ScholarshipSyncLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScholarshipSyncLog(PathMetadata metadata) {
        super(ScholarshipSyncLog.class, metadata);
    }

}

