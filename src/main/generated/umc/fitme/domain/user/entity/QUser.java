package umc.fitme.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1457923573L;

    public static final QUser user = new QUser("user");

    public final umc.fitme.global.entity.QBaseEntity _super = new umc.fitme.global.entity.QBaseEntity(this);

    public final NumberPath<Integer> applyCount = createNumber("applyCount", Integer.class);

    public final DatePath<java.time.LocalDate> birth = createDate("birth", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isOnboarded = createBoolean("isOnboarded");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final EnumPath<umc.fitme.domain.user.enums.SocialType> socialType = createEnum("socialType", umc.fitme.domain.user.enums.SocialType.class);

    public final StringPath socialUid = createString("socialUid");

    public final BooleanPath termsAgreed = createBoolean("termsAgreed");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

