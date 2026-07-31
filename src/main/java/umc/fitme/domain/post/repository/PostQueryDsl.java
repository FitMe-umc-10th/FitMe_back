package umc.fitme.domain.post.repository;

import umc.fitme.domain.post.dto.PostSearchDto;
import umc.fitme.domain.post.entity.Post;

import java.util.List;

public interface PostQueryDsl {

    List<Post> searchPostByCondition(PostSearchDto.PostSearchReq condition);
}
