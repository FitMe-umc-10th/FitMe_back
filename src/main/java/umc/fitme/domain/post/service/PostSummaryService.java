package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import umc.fitme.domain.post.client.OpenAiSummaryClient;
import umc.fitme.domain.post.entity.Contest;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.List;
import java.util.Set;

/**
 * 공고당 1회만 생성해 캐싱하는 일반 AI 요약과, 그 캐싱된 텍스트를 유저 프로필과 결합하는
 * 개인화 템플릿을 담당한다. 상세 조회 시점에는 AI를 호출하지 않고 이 서비스가 만든
 * 캐싱된 문자열만 조합한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSummaryService {

    private static final int BATCH_SIZE = 20;

    private final PostRepository postRepository;
    private final OpenAiSummaryClient openAiSummaryClient;

    /**
     * summary가 아직 캐싱되지 않은 활성 공고를 배치로 찾아 AI 요약을 생성해 저장한다.
     * 타입(Scholarship/Contest)에 무관하게 동작하므로, 등록 경로와 상관없이
     * 주기적으로 호출하면 모든 공고에 요약이 채워진다.
     *
     * OpenAI 호출은 트랜잭션 밖에서 수행한다. 여러 건을 순차 호출하는 동안 DB 커넥션을
     * 계속 붙잡고 있으면, 커넥션 풀 고갈이나 스케줄러 스레드 점유로 다른 스케줄된 작업이
     * 지연/누락될 수 있기 때문이다.
     */
    public int generateMissingSummaries() {
        if (!openAiSummaryClient.isConfigured()) {
            log.warn("OpenAI API 키가 설정되지 않아 AI 요약 생성을 건너뜁니다.");
            return 0;
        }

        List<Post> targets = postRepository.findByActiveTrueAndSummaryIsNull(PageRequest.of(0, BATCH_SIZE));
        int successCount = 0;
        for (Post post : targets) {
            if (generateAndSave(post.getId(), buildPrompt(post))) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 특정 공고 하나의 AI 요약을 강제로 재생성한다 (테스트/수동 트리거용).
     */
    public void generateSummaryForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

        if (!openAiSummaryClient.isConfigured()) {
            log.warn("OpenAI API 키가 설정되지 않아 AI 요약 생성을 건너뜁니다. postId={}", postId);
            return;
        }

        generateAndSave(postId, buildPrompt(post));
    }

    private boolean generateAndSave(Long postId, String prompt) {
        try {
            String summary = openAiSummaryClient.generateSummary(prompt);
            saveSummary(postId, summary);
            log.info("AI 요약 생성 완료 - postId={}", postId);
            return true;
        } catch (Exception e) {
            log.warn("AI 요약 생성 실패 - postId={}, reason={}", postId, e.getMessage());
            return false;
        }
    }

    /**
     * OpenAI 응답을 받은 뒤 DB 쓰기만 짧게 수행한다.
     * postRepository.save(...) 자체가 자체 트랜잭션으로 처리되므로 별도로 @Transactional을
     * 두르지 않는다. (같은 클래스 내에서 @Transactional 메서드를 this로 호출하면 프록시를
     * 우회해 트랜잭션이 적용되지 않는 self-invocation 문제를 피하기 위함이기도 하다.)
     */
    private void saveSummary(Long postId, String summary) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));
        post.updateSummary(summary);
        postRepository.save(post);
    }

    /**
     * 캐싱된 일반 요약문 + 유저 프로필(이름/관심분야)을 템플릿에 삽입해 최종 요약 박스를 만든다.
     * AI를 호출하지 않는 순수 문자열 조합이다.
     */
    public String buildPersonalizedSummary(Post post, String userName, Set<String> interestFields) {
        String generalSummary = post.getSummary();
        if (!StringUtils.hasText(generalSummary)) {
            return "AI 요약을 준비 중입니다. 잠시 후 다시 확인해주세요.";
        }

        String namePart = StringUtils.hasText(userName) ? userName : "회원";
        String interestPart = (interestFields == null || interestFields.isEmpty())
                ? "다양한 분야"
                : String.join(", ", interestFields);

        return """
                %s님, %s에 관심이 있으시군요!
                %s
                """.formatted(namePart, interestPart, generalSummary).strip();
    }

    private String buildPrompt(Post post) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 공고를 요약해줘.\n");
        prompt.append("제목: ").append(post.getTitle()).append("\n");
        prompt.append("주최기관: ").append(post.getOrganizer()).append("\n");
        prompt.append("신청기간: ").append(post.getApplyStartAt()).append(" ~ ").append(post.getApplyEndAt()).append("\n");
        prompt.append("신청방법: ").append(post.getApplicationMethod()).append("\n");

        if (post instanceof Scholarship scholarship) {
            prompt.append("학점 조건: ").append(scholarship.getGradeRequirement()).append("\n");
            prompt.append("소득 조건: ").append(scholarship.getIncomeRequirement()).append("\n");
            prompt.append("지역 조건: ").append(scholarship.getRegionRequirement()).append("\n");
            prompt.append("대학 조건: ").append(scholarship.getUniversityRequirement()).append("\n");
            prompt.append("지원 금액: ").append(scholarship.getSupportAmount()).append("\n");
        } else if (post instanceof Contest contest) {
            prompt.append("공모전 분야: ").append(contest.getContestCategory()).append("\n");
            prompt.append("참가 대상: ").append(contest.getTarget()).append("\n");
            prompt.append("참가 인원: ").append(contest.getParticipantLimit()).append("\n");
            prompt.append("총 상금: ").append(contest.getRewardTotal()).append("\n");
        }

        return prompt.toString();
    }
}
