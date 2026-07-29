package umc.fitme.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import umc.fitme.domain.user.dto.ProfileImageRequestDto;
import umc.fitme.domain.user.dto.ProfileImageResponseDto;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileImageServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private ProfileImageService profileImageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileImageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(profileImageService, "region", "ap-northeast-2");
    }

    @Test
    @DisplayName("허용되지 않은 contentType이면 INVALID_IMAGE_TYPE 예외가 발생한다")
    void createPresignedUrl_invalidContentType() {
        // given
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("a.gif", "image/gif");

        // when
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> profileImageService.createPresignedUrl(1L, request)
        );

        // then
        assertEquals(UserErrorCode.INVALID_IMAGE_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("확장자가 없는 파일명이면 INVALID_IMAGE_FILE_NAME 예외가 발생한다")
    void createPresignedUrl_invalidFileName() {
        // given
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar", "image/png");

        // when
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> profileImageService.createPresignedUrl(1L, request)
        );

        // then
        assertEquals(UserErrorCode.INVALID_IMAGE_FILE_NAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상 요청이면 uploadUrl과 fileUrl을 발급한다")
    void createPresignedUrl_success() throws Exception {
        // given
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar.png", "image/png");

        URL presignedUrl = URI.create("https://test-bucket.s3.ap-northeast-2.amazonaws.com/presigned?X-Amz-Signature=test").toURL();
        PresignedPutObjectRequest presignedRequest = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(presignedUrl);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

        // when
        ProfileImageResponseDto.PresignedUrlResponse response =
                profileImageService.createPresignedUrl(1L, request);

        // then
        assertNotNull(response.uploadUrl());
        assertNotNull(response.fileUrl());
        assertEquals(presignedUrl.toString(), response.uploadUrl());
        assertTrue(response.fileUrl().startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile/"));
    }
}