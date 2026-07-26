package umc.fitme.domain.notify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umc.fitme.domain.notify.entity.Announcement;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();
}