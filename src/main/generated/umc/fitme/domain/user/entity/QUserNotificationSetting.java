package umc.fitme.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserNotificationSetting is a Querydsl query type for UserNotificationSetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserNotificationSetting extends EntityPathBase<UserNotificationSetting> {

    private static final long serialVersionUID = -1806435664L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserNotificationSetting userNotificationSetting = new QUserNotificationSetting("userNotificationSetting");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath notificationEmail = createString("notificationEmail");

    public final BooleanPath pushEnabled = createBoolean("pushEnabled");

    public final BooleanPath recommendedEnabled = createBoolean("recommendedEnabled");

    public final BooleanPath reminderEnabled = createBoolean("reminderEnabled");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QUserNotificationSetting(String variable) {
        this(UserNotificationSetting.class, forVariable(variable), INITS);
    }

    public QUserNotificationSetting(Path<? extends UserNotificationSetting> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserNotificationSetting(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserNotificationSetting(PathMetadata metadata, PathInits inits) {
        this(UserNotificationSetting.class, metadata, inits);
    }

    public QUserNotificationSetting(Class<? extends UserNotificationSetting> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

