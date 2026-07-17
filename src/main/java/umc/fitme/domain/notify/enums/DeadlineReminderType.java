package umc.fitme.domain.notify.enums;

import lombok.Getter;

@Getter
public enum DeadlineReminderType {
    D_MINUS_7(7),
    D_MINUS_3(3),
    D_MINUS_1(1);

    private final int daysBefore;

    DeadlineReminderType(int daysBefore) {
        this.daysBefore = daysBefore;
    }
}
