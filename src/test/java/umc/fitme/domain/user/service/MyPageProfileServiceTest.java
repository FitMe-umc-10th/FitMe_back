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
import umc.fitme.domain.user.dto.MyPageProfileRequestDto;
import umc.fitme.domain.user.dto.MyPageProfileResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    /* ===================== updateProfile ===================== */

    @Nested
    @DisplayName("updateProfile 은 값이 온 필드만 병합해 수정한다")
    class UpdateProfile {

        @Test
        @DisplayName("gpa 만 전달하면 나머지 스칼라·이미지·관심분야는 기존값을 유지한다")
        void 진짜_부분수정_병합() {
            // given: gpa 만 오고 나머지는 null
            User user = user();
            UserDetail detail = detail(user); // gpa=3.5, incomeBracket=5, region="서울", img="http://img/old.png"
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.of(detail));
            // interests 는 오지 않으므로 응답 조립용 전체 목록/선택 조회만 스텁
            given(interestRepository.findAllByOrderByIdAsc()).willReturn(allInterests());
            given(userInterestRepository.findAllByUser(user)).willReturn(List.of());

            MyPageProfileRequestDto.UpdateProfileRequest request =
                    new MyPageProfileRequestDto.UpdateProfileRequest(
                            new BigDecimal("4.00"), null, null, null, null);

            // when
            MyPageProfileResponseDto.UpdateProfileResponse response =
                    myPageProfileService.updateProfile(USER_ID, request);

            // then: 실제 객체의 최종 상태로 검증 (gpa 만 바뀌고 나머지는 유지)
            assertThat(detail.getGpa()).isEqualTo(4.00f);
            assertThat(detail.getIncomeBracket()).isEqualTo(5);
            assertThat(detail.getRegion()).isEqualTo("서울");
            assertThat(detail.getProfileImageUrl()).isEqualTo("http://img/old.png");

            assertThat(response.gpa()).isEqualTo(4.00f);
            assertThat(response.incomeBracket()).isEqualTo(5);
            assertThat(response.region()).isEqualTo("서울");
            assertThat(response.profileImageUrl()).isEqualTo("http://img/old.png");

            // then: 관심분야 교체 로직은 타지 않는다
            verify(userInterestRepository, never()).deleteAllByUser(any());
            verify(userInterestRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("모든 필드를 함께 전달하면 스칼라·이미지가 갱신되고 관심분야가 전체 교체되며 추천이 재계산된다")
        void 동시_수정() {
            // given: 모든 필드 전달, 관심분야는 마케팅(1)·디자인(3) 선택
            User user = user();
            UserDetail detail = detail(user);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.of(detail));

            List<Long> requestedIds = List.of(1L, 3L);
            given(interestRepository.findAllById(requestedIds))
                    .willReturn(List.of(marketing(), design()));
            // 응답 조립: 전체 목록 + 교체 후 선택 상태(마케팅·디자인)
            given(interestRepository.findAllByOrderByIdAsc()).willReturn(allInterests());
            given(userInterestRepository.findAllByUser(user)).willReturn(List.of(
                    userInterest(user, marketing()),
                    userInterest(user, design())
            ));

            MyPageProfileRequestDto.UpdateProfileRequest request =
                    new MyPageProfileRequestDto.UpdateProfileRequest(
                            new BigDecimal("2.75"), 8, "부산", requestedIds, "http://img/new.png");

            // when
            MyPageProfileResponseDto.UpdateProfileResponse response =
                    myPageProfileService.updateProfile(USER_ID, request);

            // then: 실제 객체 최종 상태가 전부 새 값
            assertThat(detail.getGpa()).isEqualTo(2.75f);
            assertThat(detail.getIncomeBracket()).isEqualTo(8);
            assertThat(detail.getRegion()).isEqualTo("부산");
            assertThat(detail.getProfileImageUrl()).isEqualTo("http://img/new.png");

            assertThat(response.gpa()).isEqualTo(2.75f);
            assertThat(response.incomeBracket()).isEqualTo(8);
            assertThat(response.region()).isEqualTo("부산");
            assertThat(response.profileImageUrl()).isEqualTo("http://img/new.png");

            // then: 관심분야 전체 교체 (삭제 후 저장)
            verify(userInterestRepository).deleteAllByUser(user);
            verify(userInterestRepository).saveAll(anyList());

            // then: 응답 interests 는 전체 목록 + 요청한 것만 selected=true (순서 보장)
            assertThat(response.interests())
                    .extracting(MyPageProfileResponseDto.InterestItem::interestId)
                    .containsExactly(1L, 2L, 3L);
            assertThat(response.interests())
                    .extracting(MyPageProfileResponseDto.InterestItem::selected)
                    .containsExactly(true, false, true);

            // then: 추천 재계산 정확히 1회
            verify(recommendationRefreshService, times(1)).refresh(USER_ID);
        }
    }
}