package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserNotificationSetting;

import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
    Optional<UserNotificationSetting> findByUser(User user);
}