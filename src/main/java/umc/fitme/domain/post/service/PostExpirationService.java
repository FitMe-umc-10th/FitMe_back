package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.entity.Post;
import umc.fitme.domain.post.repository.PostRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostExpirationService {

    private final PostRepository postRepository;

    @Transactional
    public int deactivateExpiredPosts() {
        List<Post> expiredPosts = postRepository.findAllActiveAndExpired();
        expiredPosts.forEach(Post::deactivate);

        log.info("마감된 공고 비활성화 완료 - 대상 {}건", expiredPosts.size());
        return expiredPosts.size();
    }
}
