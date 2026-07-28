package umc.fitme.domain.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InquiryRequestDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("정상 요청이면 제약 위반이 없다")
    void valid_noViolation() {
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", "문의 내용입니다.");

        Set<ConstraintViolation<InquiryRequestDto.CreateInquiryRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 제약 위반이 발생한다")
    void invalidEmail_violation() {
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("not-an-email", "문의 내용입니다.");

        Set<ConstraintViolation<InquiryRequestDto.CreateInquiryRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("replyEmail")));
    }

    @Test
    @DisplayName("문의 내용이 501자면 제약 위반이 발생한다")
    void contentTooLong_violation() {
        String content = "가".repeat(501);
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("reply@example.com", content);

        Set<ConstraintViolation<InquiryRequestDto.CreateInquiryRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    @DisplayName("빈 값이면 replyEmail/content 모두 제약 위반이 발생한다")
    void blankValues_violation() {
        InquiryRequestDto.CreateInquiryRequest request =
                new InquiryRequestDto.CreateInquiryRequest("", "");

        Set<ConstraintViolation<InquiryRequestDto.CreateInquiryRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("replyEmail")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }
}