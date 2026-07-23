package umc.fitme.domain.post.service;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.converter.PostConverter;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.repository.PostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;


    public PostResponseDTO.PopularPostListDTO getPopularPosts(Long cursor, Integer size) {



        PageRequest pageRequest = PageRequest.of(0, size);
        List<Post> randomPosts = postRepository.findRandomPosts(pageRequest);


        boolean hasNext = false;
        Long nextCursor = null;


        return PostConverter.toPopularPostListDTO(randomPosts, hasNext, nextCursor);
    }


    public PostResponseDTO.PostPreviewListDTO getRecentPosts(Long cursor, Integer size) {
        return PostResponseDTO.PostPreviewListDTO.builder()
                .posts(List.of())
                .hasNext(false)
                .nextCursor(null)
                .build();
    }


    public List<PostResponseDTO.PostPreviewDTO> getClosingSoonPosts(Integer size) {


        MockUserFitInfo currentUser = MockUserFitInfo.builder()
                .gpa(3.5)
                .incomeBracket("3구간")
                .region("서울")
                .university("한국대학교")
                .interestField("IT/소프트웨어")
                .build();


        PageRequest candidateRequest = PageRequest.of(0, 50);
        List<Post> candidatePosts = postRepository.findClosingSoonPosts(candidateRequest);


        List<Post> matchedPosts = candidatePosts.stream()
                .filter(post -> isUserFit(post, currentUser))
                .limit(size)
                .collect(Collectors.toList());

        if (matchedPosts.isEmpty()) {
            PageRequest coldStartRequest = PageRequest.of(0, 5); // 5건 디폴트 노출
            matchedPosts = postRepository.findPopularPostsBySavedCount(coldStartRequest);
        }


        List<Long> dummySavedPostIds = List.of(1L, 3L);

        return matchedPosts.stream()
                .map(post -> {
                    boolean isSaved = dummySavedPostIds.contains(post.getId());
                    return PostConverter.toPostPreviewDTO(post, isSaved);
                })
                .collect(Collectors.toList());
    }


    private boolean isUserFit(Post post, MockUserFitInfo user) {


        if (post instanceof Scholarship scholarship) {


            try {
                double requiredGpa = Double.parseDouble(scholarship.getGradeRequirement().replaceAll("[^0-9.]", ""));
                if (user.getGpa() < requiredGpa) return false; // 미달이면 탈락
            } catch (Exception e) {

            }


            if (scholarship.getIncomeRequirement() != null && !scholarship.getIncomeRequirement().contains("제한없음")) {
                if (!scholarship.getIncomeRequirement().contains(user.getIncomeBracket())) return false;
            }


            if (scholarship.getRegionRequirement() != null && !scholarship.getRegionRequirement().contains("전국")) {
                if (!scholarship.getRegionRequirement().contains(user.getRegion())) return false;
            }
        }


        return true;
    }


    @Getter
    @Builder
    private static class MockUserFitInfo {
        private double gpa;            // 학점
        private String incomeBracket;  // 소득구간
        private String region;         // 거주지역
        private String university;     // 소속대학
        private String interestField;  // 관심분야
    }

}