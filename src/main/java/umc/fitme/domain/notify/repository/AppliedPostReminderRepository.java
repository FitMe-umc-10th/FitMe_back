package umc.fitme.domain.notify.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.notify.dto.AppliedPostReminderTarget;
import umc.fitme.domain.user.entity.mapping.UserApplication;

import java.time.LocalDate;
import java.util.List;

public interface AppliedPostReminderRepository extends Repository<UserApplication, Long> {

    @Query("""
            select new umc.fitme.domain.notify.dto.AppliedPostReminderTarget(u, p)
            from UserApplication ua
            join ua.user u
            join ua.post p
            join UserNotificationSetting uns on uns.user = u
            where ua.deletedAt is null
              and p.applyEndAt in :applyEndDates
              and uns.reminderEnabled = true
            """)
    List<AppliedPostReminderTarget> findDeadlineReminderTargets(
            @Param("applyEndDates") List<LocalDate> applyEndDates
    );
}
