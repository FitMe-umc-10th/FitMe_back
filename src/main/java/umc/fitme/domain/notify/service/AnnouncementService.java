package umc.fitme.domain.notify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.notify.converter.AnnouncementConverter;
import umc.fitme.domain.notify.dto.AnnouncementResponseDto;
import umc.fitme.domain.notify.entity.Announcement;
import umc.fitme.domain.notify.exception.code.AnnouncementErrorCode;
import umc.fitme.domain.notify.repository.AnnouncementRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private static final int NEW_THRESHOLD_DAYS = 3; 

    private final AnnouncementRepository announcementRepository;
    private final Clock clock;

    public AnnouncementResponseDto.AnnouncementListResponse getAnnouncements() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();

        return AnnouncementConverter.toListResponse(announcements, now, NEW_THRESHOLD_DAYS);
    }

    public AnnouncementResponseDto.AnnouncementDetailResponse getAnnouncementDetail(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ProjectException(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND));

        return AnnouncementConverter.toDetailResponse(announcement);
    }
}