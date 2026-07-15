package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.interest.entity.Interest;
import umc.fitme.domain.interest.entity.mapping.UserInterest;
import umc.fitme.domain.interest.repository.InterestRepository;
import umc.fitme.domain.interest.repository.UserInterestRepository;
import umc.fitme.domain.user.dto.MyPageProfileResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MyPageProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailRepository userDetailRepository;
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private UserInterestRepository userInterestRepository;
    @Mock
    private RecommendationRefreshService recommendationRefreshService;

    @InjectMocks
    private MyPageProfileService myPageProfileService;

    private static final Long USER_ID = 1L;

    /* ===================== 공통 픽스처 ===================== */

    private User user() {
        return User.builder()
                .id(USER_ID)
                .email("test@fitme.com")
                .name("홍길동")
                .build();
    }

    private UserDetail detail(User user) {
        return UserDetail.builder()
                .id(10L)
                .user(user)
                .gpa(3.5f)
                .incomeBracket(5)
                .region("서울")
                .universityName("핏미대학교")
                .profileImageUrl("http://img/old.png")
                .build();
    }

    private Interest marketing() {
        return Interest.builder().id(1L).interestName("마케팅").build();
    }

    private Interest planning() {
        return Interest.builder().id(2L).interestName("기획").build();
    }

    private Interest design() {
        return Interest.builder().id(3L).interestName("디자인").build();
    }

    /** id 오름차순으로 정렬된 전체 관심분야 (findAllByOrderByIdAsc 스텁 반환값) */
    private List<Interest> allInterests() {
        return List.of(marketing(), planning(), design());
    }

    private UserInterest userInterest(User user, Interest interest) {
        return UserInterest.builder()
                .user(user)
                .interest(interest)
                .build();
    }

    /* ===================== getProfile ===================== */

    @Nested
    @DisplayName("getProfile 은 프로필과 전체 관심분야를 조회한다")
    class GetProfile {

        @Test
        @DisplayName("정상 조회 시 프로필 필드가 모두 채워지고 전체 관심분야 중 선택한 것만 selected=true 로 반환한다")
        void 정상_조회() {
            // given
            User user = user();
            UserDetail detail = detail(user);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.of(detail));
            given(interestRepository.findAllByOrderByIdAsc()).willReturn(allInterests());
            // 마케팅(1), 디자인(3) 만 선택
            given(userInterestRepository.findAllByUser(user)).willReturn(List.of(
                    userInterest(user, marketing()),
                    userInterest(user, design())
            ));

            // when
            MyPageProfileResponseDto.ProfileResponse response = myPageProfileService.getProfile(USER_ID);

            // then: 프로필 스칼라 필드
            assertThat(response.name()).isEqualTo("홍길동");
            assertThat(response.universityName()).isEqualTo("핏미대학교");
            assertThat(response.profileImageUrl()).isEqualTo("http://img/old.png");
            assertThat(response.gpa()).isEqualTo(3.5f);
            assertThat(response.incomeBracket()).isEqualTo(5);
            assertThat(response.region()).isEqualTo("서울");

            // then: 전체 관심분야를 id 오름차순 그대로 반환 (순서까지 검증)
            assertThat(response.interests())
                    .extracting(MyPageProfileResponseDto.InterestItem::interestId)
                    .containsExactly(1L, 2L, 3L);
            assertThat(response.interests())
                    .extracting(MyPageProfileResponseDto.InterestItem::interestName)
                    .containsExactly("마케팅", "기획", "디자인");
            assertThat(response.interests())
                    .extracting(MyPageProfileResponseDto.InterestItem::selected)
                    .containsExactly(true, false, true);

            // then: 정렬 보장 쿼리를 쓰고 정렬 미보장 findAll 은 호출하지 않는다
            verify(interestRepository).findAllByOrderByIdAsc();
            verify(interestRepository, never()).findAll();
        }

        @Test
        @DisplayName("사용자가 없으면 USER_NOT_FOUND ProjectException 을 던진다")
        void 사용자_없음() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> myPageProfileService.getProfile(USER_ID))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("UserDetail 이 없으면 USER_DETAIL_NOT_FOUND ProjectException 을 던진다")
        void 유저디테일_없음() {
            // given
            User user = user();
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> myPageProfileService.getProfile(USER_ID))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(UserErrorCode.USER_DETAIL_NOT_FOUND);
        }
    }
}