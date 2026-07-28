package umc.fitme.domain.notify.converter;

import umc.fitme.domain.notify.dto.AnnouncementResponseDto;
import umc.fitme.domain.notify.entity.Announcement;
import umc.fitme.domain.notify.util.RelativeTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementConverter {

    public static AnnouncementResponseDto.AnnouncementItem toItem(
            Announcement a,
            LocalDateTime now,
            int newThresholdDays
    ) {
        return AnnouncementResponseDto.AnnouncementItem.builder()
                .announcementId(a.getId())
                .category(a.getAnnouncementCategory().name())
                .categoryName(a.getAnnouncementCategory().getCategoryName())
                .title(a.getTitle())
                .createdAt(a.getCreatedAt())
                .createdAtString(RelativeTimeFormatter.format(a.getCreatedAt(), now))
                .isNew(a.getCreatedAt().isAfter(now.minusDays(newThresholdDays)))
                .build();
    }

    public static AnnouncementResponseDto.AnnouncementListResponse toListResponse(
            List<Announcement> announcements,
            LocalDateTime now,
            int newThresholdDays
    ) {
        List<AnnouncementResponseDto.AnnouncementItem> items = announcements.stream()
                .map(a -> toItem(a, now, newThresholdDays))
                .toList();

        return AnnouncementResponseDto.AnnouncementListResponse.builder()
                .announcements(items)
                .build();
    }

    public static AnnouncementResponseDto.AnnouncementDetailResponse toDetailResponse(Announcement a) {
        return AnnouncementResponseDto.AnnouncementDetailResponse.builder()
                .announcementId(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .createdAt(a.getCreatedAt())
                .build();
    }
}