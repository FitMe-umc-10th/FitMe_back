package umc.fitme.domain.user.dto;

import umc.fitme.domain.user.enums.Status;

public class UserApplicationRequestDto {

    /* POST 자동등록 */
    public record CreateRequest(
            Long postId
    ) {
    }

    /* PATCH status */
    public record UpdateStatusRequest(
            Status status
    ) {
    }

    /* PATCH memo */
    public record UpdateMemoRequest(
            String memo
    ) {
    }
}
