package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.converter.PostConverter;
import umc.fitme.domain.post.dto.response.PostResponseDTO;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 데이터 변경이 없는 조회 메서드이므로 readOnly = true 설정
public class PostQueryService {

    private final PostRepository postRepository;

    // 💡 기존에 고정해둔 PAGE_SIZE 변수를 지우고, 컨트롤러에서 넘겨주는 size를 받도록 파라미터를 추가
    public PostResponseDTO.PopularPostListDTO getPopularPosts(Long cursor, Integer size) {

        // 1. 다음 페이지가 있는지 확인하기 위해 요청 사이즈(size)보다 1개 더 많은 (size + 1)개를 조회
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        // 2. Repository 호출 (작성해두신 최신순 커서 쿼리 사용)[cite: 2]
        List<Post> postList = postRepository.findPopularPosts(cursor, pageRequest);

        // 3. hasNext 판별 및 nextCursor 추출[cite: 2]
        boolean hasNext = postList.size() > size;
        Long nextCursor = null;

        if (hasNext) {
            // 다음 페이지가 있는지를 확인하고 사이즈를 조절
            postList.remove(size.intValue());
        }

        // 리스트가 비어있지 않다면, 마지막 요소의 ID를 다음 커서로 지정[cite: 2]
        if (!postList.isEmpty()) {
            nextCursor = postList.get(postList.size() - 1).getId();
        }

        // 4. Converter를 통해 Entity 리스트를 DTO로 변환하여 컨트롤러로 반환[cite: 2]
        return PostConverter.toPopularPostListDTO(postList, hasNext, nextCursor);
    }
}