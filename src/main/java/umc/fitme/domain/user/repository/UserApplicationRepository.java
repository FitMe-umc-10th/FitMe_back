package umc.fitme.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.mapping.UserApplication;
import umc.fitme.domain.user.enums.Status;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {
    Optional<UserApplication> findByUserAndPost(User user, Post post);
    @EntityGraph(attributePaths = "post")
    List<UserApplication> findAllByUserAndStatusInOrderByUpdatedAtDescIdDesc(
            User user,
            List<Status> statuses
    );

    Optional<UserApplication> findByIdAndUser(Long id, User user);

    /**
     * 사용자의 지원 완료 건수를 조회합니다.
     *
     * @param user 지원 내역을 조회할 사용자
     * @param statuses 지원 완료로 판단할 상태 목록
     * @return 사용자의 지원 완료 건수
     */
    long countByUserAndStatusIn(User user, List<Status> statuses);

    /**
     * 사용자의 결과 대기 중인 지원 건수를 조회합니다.
     *
     * @param user 지원 내역을 조회할 사용자
     * @param status 결과 대기 상태
     * @return 사용자의 대기 중인 지원 건수
     */
    long countByUserAndStatus(User user, Status status);

    /**
     * 최종 합격한 장학금의 지원 금액 목록을 조회합니다.
     *
     * @param user 지원 내역을 조회할 사용자
     * @param status 최종 합격 상태
     * @return 최종 합격한 장학금의 지원 금액 목록
     */
    // Scholarship은 Post를 상속하며 동일한 post_id를 공유하므로 id 기준으로 조인
    @Query("SELECT s.supportAmount FROM UserApplication ua " +
           "JOIN Scholarship s ON s.id = ua.post.id " +
           "WHERE ua.user = :user AND ua.status = :status")
    List<String> findFinalPassedScholarshipAmounts(@Param("user") User user, @Param("status") Status status);
}
