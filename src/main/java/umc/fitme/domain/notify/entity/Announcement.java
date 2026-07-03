package umc.fitme.domain.notify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.fitme.domain.notify.enums.AnnouncementCategory;
import umc.fitme.global.entity.BaseEntity;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "annoucement")
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private AnnouncementCategory announcementCategory;

    @Column(name = "title", nullable = false)
    private String title;
}
