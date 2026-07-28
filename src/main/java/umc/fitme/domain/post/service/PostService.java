package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.converter.PostConverter;
import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.dto.SearchViewDto;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.FluctuationType;
import umc.fitme.domain.post.enums.SearchSortType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.repository.SearchRecentRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserSaveRepository userSaveRepository;
    private final SearchRecentRepository searchRecentRepository;

    /***
     * 조건에 맞는 공고를 탐색하고 반환한다.
     * @param dto
     * @param userId
     * @return
     */
    public PostSearchDto.Pagination<PostSearchDto.PostSearchRes> searchPost(PostSearchDto.PostSearchReq dto, Long userId){

        // 조건에 맞는 Post 반환
        List<Post> postList = postRepository.searchPostByCondition(dto);
        int pageSize = dto.pageSize();

        // hasNext 존재 여부 확인
        boolean hasNext = false;
        if (postList.size() > dto.pageSize()){
            hasNext = true;
            postList.remove(pageSize);
        }

        // 다음 커서 반환
        Long nextIdCursor = null;
        LocalDate nextDeadlineCursor = null;
        // 마감일 정렬일 경우 -> idCursor, deadlineCursor 두 개 반환
        if (dto.sort() == null || dto.sort() == SearchSortType.DEADLINE){
            if (!postList.isEmpty()){
                nextIdCursor = postList.getLast().getId();
                nextDeadlineCursor = postList.getLast().getApplyEndAt();
            }
        } else { // 최신순 정렬일 경우 -> idCursor만 반환
            if (!postList.isEmpty()){
                nextIdCursor = postList.getLast().getId();
            }
        }

        // 가져온 공고 리스트가 saved인지를 반환 로직
        List<Long> postIds = postList.stream().map(Post::getId).toList();
        Set<Long> savedPostIds = userSaveRepository.findUserSaveIdsByUserIdAndPostIds(userId, postIds);

        List<PostSearchDto.PostSearchRes> postResList = postList.stream()
                .map(post -> PostConverter.toPostSearchRes(post, savedPostIds.contains(post.getId())))
                .toList();

        return PostConverter.toPagination(
                postResList,
                hasNext,
                nextIdCursor,
                nextDeadlineCursor,
                pageSize
        );
    }

    /***
     *
     * @param userId
     * @return
     */
    public SearchViewDto.SearchViewRes getSearchMainPage(Long userId) {

        // 최근 10개의 검색어 조회
        List<SearchViewDto.RecentKeywordDto> recentKeywords = searchRecentRepository.findTop10ByUserId(userId)
                .stream()
                .map(PostConverter::toRecentKeywordDto)
                .toList();

        // 실시간 공고 8개 조회
        List<Post> top8Posts = postRepository.findTop8ByViewCount();
        List<SearchViewDto.RealtimePostDto> realtimePostDto = new ArrayList<>();
        for (int i = 0; i < top8Posts.size(); i++){
            Post post = top8Posts.get(i);
            int newRank = i+1;

            // 순위 변동 계산
            int prevRank = post.getPostRank();
            FluctuationType fluctuationType = getFluctuation(newRank, prevRank);

            SearchViewDto.RealtimePostDto realtimePost = PostConverter.toRealtimePostDto(post, newRank, fluctuationType);
            realtimePostDto.add(realtimePost);
        }

        SearchViewDto.RealtimePostGroupDto realtimePostGroup = SearchViewDto.RealtimePostGroupDto.builder()
                .baseTime(LocalDateTime.now())
                .posts(realtimePostDto)
                .build();

        return SearchViewDto.SearchViewRes.builder()
                .recentKeywords(recentKeywords)
                .realtimePosts(realtimePostGroup)
                .build();
    }

    // 순위 변동 계산 로직
    private FluctuationType getFluctuation(int newRank, int prevRank){

        // 이전 순위 정보가 없을 경우 -> 새롭게 순위 진입
        if (prevRank == -1){
            return FluctuationType.NEW;
        }

        // 기존 순위보다 상승했을 경우
        if (newRank < prevRank){
            return FluctuationType.UP;
        } else if (newRank > prevRank){ // 기존 순위보다 하락했을 경우
            return FluctuationType.DOWN;
        } else {
            return FluctuationType.SAME;
        }
    }
}
