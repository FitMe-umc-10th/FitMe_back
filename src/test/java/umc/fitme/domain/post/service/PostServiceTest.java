package umc.fitme.domain.post.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.user.entity.SearchRecent;
import umc.fitme.domain.user.entity.User;
import umc.fitme.domain.user.repository.SearchRecentRepository;
import umc.fitme.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SearchRecentRepository searchRecentRepository;

    @Test
    @DisplayName("최근 검색어가 정상적으로 삭제된다.")
    void delete_recentSearch(){
        // given
        User savedUser = userRepository.save(
                User.builder().email("example@fitme.com").name("유저1").build()
        );

        SearchRecent savedSearchRecent = searchRecentRepository.save(
                SearchRecent.builder().user(savedUser).keyword("키워드").updateAt(LocalDateTime.now()).build()
        );

        // when
        postService.deleteRecentKeyword(savedUser.getId(), savedSearchRecent.getId());

        // then
        Optional<SearchRecent> deletedSearch = searchRecentRepository.findById(savedSearchRecent.getId());
        Assertions.assertThat(deletedSearch).isEmpty();
    }
}