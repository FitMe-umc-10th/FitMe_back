package umc.fitme.domain.notify.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDeadlineEmailNotificationLog is a Querydsl query type for DeadlineEmailNotificationLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeadlineEmailNotificationLog extends EntityPathBase<DeadlineEmailNotificationLog> {

    private static final long serialVersionUID = 1211937213L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDeadlineEmailNotificationLog deadlineEmailNotificationLog = new QDeadlineEmailNotificationLog("deadlineEmailNotificationLog");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> applyEndAt = createDate("applyEndAt", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final umc.fitme.domain.post.entity.QPost post;

    public final EnumPath<umc.fitme.domain.notify.enums.DeadlineReminderType> reminderType = createEnum("reminderType", umc.fitme.domain.notify.enums.DeadlineReminderType.class);

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public final EnumPath<umc.fitme.domain.notify.enums.EmailSendStatus> status = createEnum("status", umc.fitme.domain.notify.enums.EmailSendStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final umc.fitme.domain.user.entity.QUser user;

    public QDeadlineEmailNotificationLog(String variable) {
        this(DeadlineEmailNotificationLog.class, forVariable(variable), INITS);
    }

    public QDeadlineEmailNotificationLog(Path<? extends DeadlineEmailNotificationLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDeadlineEmailNotificationLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDeadlineEmailNotificationLog(PathMetadata metadata, PathInits inits) {
        this(DeadlineEmailNotificationLog.class, metadata, inits);
    }

    public QDeadlineEmailNotificationLog(Class<? extends DeadlineEmailNotificationLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new umc.fitme.domain.post.entity.QPost(forProperty("post")) : null;
        this.user = inits.isInitialized("user") ? new umc.fitme.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

