package umc.fitme.domain.interest.entity.mapping;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import umc.fitme.domain.interest.entity.Interest;
import umc.fitme.domain.user.entity.User;
import umc.fitme.global.entity.BaseEntity;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "user_interest",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest_id"}))
public class UserInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;
}
