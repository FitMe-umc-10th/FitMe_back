package umc.fitme.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import umc.fitme.domain.user.dto.ProfileImageRequestDto;
import umc.fitme.domain.user.dto.ProfileImageResponseDto;
import umc.fitme.domain.user.exception.code.UserErrorCode;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileImageServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private ProfileImageService profileImageService;

    private static final Long USER_ID = 1L;
    private static final String BUCKET = "test-bucket";
    private static final String REGION = "ap-northeast-2";
    private static final long VALID_FILE_SIZE = 1024L;
    private static final String PRESIGNED_URL =
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/presigned?X-Amz-Signature=test";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileImageService, "bucket", BUCKET);
        ReflectionTestUtils.setField(profileImageService, "region", REGION);
    }

    /* presignPutObject 가 임의의 URL 을 돌려주도록 스텁 (서명 검증은 캡처로 따로 확인) */
    private void stubPresigner() throws Exception {
        URL url = URI.create(PRESIGNED_URL).toURL();
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(url);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);
    }

    @Test
    @DisplayName("허용되지 않은 contentType이면 INVALID_IMAGE_TYPE 예외가 발생한다")
    void createPresignedUrl_invalidContentType() {
        // given
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("a.gif", "image/gif", VALID_FILE_SIZE);

        // when
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> profileImageService.createPresignedUrl(USER_ID, request)
        );

        // then
        assertEquals(UserErrorCode.INVALID_IMAGE_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("확장자와 contentType이 서로 어긋나면 INVALID_IMAGE_TYPE 예외가 발생한다")
    void createPresignedUrl_extensionContentTypeMismatch() {
        // given: 확장자는 png 인데 contentType 은 jpeg
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar.png", "image/jpeg", VALID_FILE_SIZE);

        // when
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> profileImageService.createPresignedUrl(USER_ID, request)
        );

        // then
        assertEquals(UserErrorCode.INVALID_IMAGE_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("확장자가 없는 파일명이면 INVALID_IMAGE_FILE_NAME 예외가 발생한다")
    void createPresignedUrl_invalidFileName() {
        // given
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar", "image/png", VALID_FILE_SIZE);

        // when
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> profileImageService.createPresignedUrl(USER_ID, request)
        );

        // then
        assertEquals(UserErrorCode.INVALID_IMAGE_FILE_NAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일 용량이 상한(5MB)을 넘으면 INVALID_IMAGE_SIZE 예외가 발생한다")
    void createPresignedUrl_fileSizeExceeded() {
        // given: 5MB + 1 byte
        long overMaxSize = 5L * 1024 * 1024 + 1;
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar.png", "image/png", overMaxSize);

        // when
        ProjectException exception = assertThrows(
                ProjectException.class,
                () -> profileImageService.createPresignedUrl(USER_ID, request)
        );

        // then
        assertEquals(UserErrorCode.INVALID_IMAGE_SIZE, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상 요청이면 uploadUrl과 fileUrl을 발급하고 key가 profile/{userId}/ 로 시작한다")
    void createPresignedUrl_success() throws Exception {
        // given
        stubPresigner();
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar.png", "image/png", VALID_FILE_SIZE);

        // when
        ProfileImageResponseDto.PresignedUrlResponse response =
                profileImageService.createPresignedUrl(USER_ID, request);

        // then
        assertNotNull(response.uploadUrl());
        assertNotNull(response.fileUrl());
        assertEquals(PRESIGNED_URL, response.uploadUrl());
        assertTrue(response.fileUrl().startsWith(
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile/1/"));
        assertTrue(response.fileUrl().endsWith(".png"));
    }

    @Test
    @DisplayName("확장자가 대문자여도 소문자로 정규화되어 정상 발급된다")
    void createPresignedUrl_uppercaseExtension() throws Exception {
        // given
        stubPresigner();
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar.PNG", "image/png", VALID_FILE_SIZE);

        // when
        profileImageService.createPresignedUrl(USER_ID, request);

        // then: key 의 확장자가 소문자 png 로 붙는다
        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner, times(1)).presignPutObject(captor.capture());

        String key = captor.getValue().putObjectRequest().key();
        assertTrue(key.endsWith(".png"), "확장자가 소문자로 정규화되어야 한다: " + key);
    }

    @Test
    @DisplayName("서명 대상 PutObjectRequest에 bucket/key/contentType/contentLength와 5분 만료가 실린다")
    void createPresignedUrl_signedRequestContents() throws Exception {
        // given
        stubPresigner();
        ProfileImageRequestDto.PresignedUrlRequest request =
                new ProfileImageRequestDto.PresignedUrlRequest("avatar.png", "image/png", VALID_FILE_SIZE);

        // when
        ProfileImageResponseDto.PresignedUrlResponse response =
                profileImageService.createPresignedUrl(USER_ID, request);

        // then
        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner, times(1)).presignPutObject(captor.capture());

        PutObjectPresignRequest captured = captor.getValue();
        assertEquals(Duration.ofMinutes(5), captured.signatureDuration());

        PutObjectRequest putObjectRequest = captured.putObjectRequest();
        assertEquals(BUCKET, putObjectRequest.bucket());
        assertEquals("image/png", putObjectRequest.contentType());
        assertEquals(VALID_FILE_SIZE, putObjectRequest.contentLength());

        String key = putObjectRequest.key();
        assertTrue(key.startsWith("profile/" + USER_ID + "/"), "key 에 본인 userId 가 포함되어야 한다: " + key);
        assertTrue(key.endsWith(".png"));

        // 응답의 fileUrl 은 서명에 사용된 key 와 동일한 객체를 가리켜야 한다
        assertTrue(response.fileUrl().endsWith("/" + key));
    }
}