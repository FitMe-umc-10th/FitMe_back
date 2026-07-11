package umc.fitme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umc.fitme.domain.user.dto.MyPageResponseDto;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.entity.UserDetail;
import umc.fitme.domain.user.enums.Status;
import umc.fitme.domain.user.repository.UserApplicationRepository;
import umc.fitme.domain.user.repository.UserDetailRepository;
import umc.fitme.domain.user.repository.UserRepository;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailRepository userDetailRepository;
    @Mock
    private UserApplicationRepository userApplicationRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private User user;
    @Mock
    private UserDetail userDetail;

    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("getMyPage")
    class GetMyPage {

        @Test
        @DisplayName("사용자가 없으면 USER_NOT_FOUND 예외를 던진다")
        void 사용자가_없으면_예외를_던진다() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> myPageService.getMyPage(USER_ID))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(GeneralErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("UserDetail이 없으면 USER_DETAIL_NOT_FOUND ProjectException을 던진다")
        void userDetail이_없으면_예외를_던진다() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then: 현재 코드가 던지는 예외/에러코드 그대로 검증
            assertThatThrownBy(() -> myPageService.getMyPage(USER_ID))
                    .isInstanceOf(ProjectException.class)
                    .extracting(e -> ((ProjectException) e).getErrorCode())
                    .isEqualTo(GeneralErrorCode.USER_DETAIL_NOT_FOUND);
        }

        @Test
        @DisplayName("완료 건수는 NONE을 제외한 완료 상태 집합으로 집계한다")
        void 완료건수_집계시_NONE은_제외된다() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.of(userDetail));

            ArgumentCaptor<List<Status>> statusesCaptor = ArgumentCaptor.forClass(List.class);
            given(userApplicationRepository.countByUserAndStatusIn(eq(user), statusesCaptor.capture()))
                    .willReturn(3L);
            given(userApplicationRepository.countByUserAndStatus(user, Status.PENDING_RESULT))
                    .willReturn(0L);
            given(userApplicationRepository.findFinalPassedScholarshipAmounts(user, Status.FINAL_PASSED))
                    .willReturn(List.of());

            // when
            MyPageResponseDto.MyPageResponse response = myPageService.getMyPage(USER_ID);

            // then: 서비스가 repo에 넘긴 완료 집합에 NONE이 없고, 반환 카운트가 그대로 매핑된다
            assertThat(statusesCaptor.getValue())
                    .containsExactlyInAnyOrder(
                            Status.PENDING_RESULT, Status.DOCUMENT_PASSED, Status.FINAL_PASSED)
                    .doesNotContain(Status.NONE);
            assertThat(response.activitySummary().completedApplicationCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("대기 건수는 PENDING_RESULT 상태로 집계한다")
        void 대기건수는_PENDING_RESULT로_집계된다() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.of(userDetail));
            given(userApplicationRepository.countByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(0L);
            given(userApplicationRepository.countByUserAndStatus(user, Status.PENDING_RESULT))
                    .willReturn(2L);
            given(userApplicationRepository.findFinalPassedScholarshipAmounts(user, Status.FINAL_PASSED))
                    .willReturn(List.of());

            // when
            MyPageResponseDto.MyPageResponse response = myPageService.getMyPage(USER_ID);

            // then
            assertThat(response.activitySummary().pendingResultCount()).isEqualTo(2L);
            verify(userApplicationRepository).countByUserAndStatus(user, Status.PENDING_RESULT);
        }

        @Test
        @DisplayName("집계결과가 0건이면 완료/대기 카운트가 모두 0이다")
        void 집계결과가_0이면_완료와_대기_카운트가_0이다() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(userDetailRepository.findByUser(user)).willReturn(Optional.of(userDetail));
            given(userApplicationRepository.countByUserAndStatusIn(eq(user), anyList()))
                    .willReturn(0L);
            given(userApplicationRepository.countByUserAndStatus(user, Status.PENDING_RESULT))
                    .willReturn(0L);
            given(userApplicationRepository.findFinalPassedScholarshipAmounts(user, Status.FINAL_PASSED))
                    .willReturn(List.of());

            // when
            MyPageResponseDto.MyPageResponse response = myPageService.getMyPage(USER_ID);

            // then
            assertThat(response.activitySummary().completedApplicationCount()).isZero();
            assertThat(response.activitySummary().pendingResultCount()).isZero();
            // TODO: 저장 포맷 확정 후 수혜액 합산 검증 추가
        }
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "5000000, 5000000",
            "'최대 1,000,000원', 1000000",
            "'1,000,000', 1000000",
            "'250만원', 2500000",
            "'최대 250만원', 2500000",
            "'1,000만원', 10000000"
    })
    void parseAmount_숫자와_콤마_표기를_long으로_변환한다(String input, long expected) {
        assertThat(MyPageService.parseAmount(input)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "\"{0}\" -> 0")
    @NullSource
    @ValueSource(strings = {"", "전액", "미정"})
    void parseAmount_숫자가_없거나_null이면_0을_반환한다(String input) {
        assertThat(MyPageService.parseAmount(input)).isEqualTo(0L);
    }
}