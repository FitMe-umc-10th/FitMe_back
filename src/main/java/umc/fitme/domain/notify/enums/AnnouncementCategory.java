package umc.fitme.domain.notify.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnnouncementCategory {
    NOTIFY("안내"),
    INSPECT("점검");

    private final String categoryName;
}