package umc.fitme.domain.notify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.notify.enums.AnnouncementCategory;
import umc.fitme.global.entity.BaseEntity;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "announcement")
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private AnnouncementCategory announcementCategory;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
}
