package umc.fitme.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@DiscriminatorValue("contest")
public class Contest extends Post {

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "target")
    private String target;

    @Column(name = "participant_limit")
    private String participantLimit;

    @Column(name = "reward_total")
    private String rewardTotal;
}
