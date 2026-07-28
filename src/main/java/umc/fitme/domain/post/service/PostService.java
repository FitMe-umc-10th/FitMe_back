package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.converter.PostConverter;
import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.enums.SearchSortType;
import umc.fitme.domain.post.repository.PostRepository;
import umc.fitme.domain.user.repository.UserSaveRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserSaveRepository userSaveRepository;

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
}
