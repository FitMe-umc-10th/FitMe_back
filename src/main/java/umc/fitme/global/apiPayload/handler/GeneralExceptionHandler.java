package umc.fitme.global.apiPayload.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import umc.fitme.global.apiPayload.ApiResponse;
import umc.fitme.global.apiPayload.code.BaseErrorCode;
import umc.fitme.global.apiPayload.code.GeneralErrorCode;
import umc.fitme.global.apiPayload.code.InvalidValueErrorCodeProvider;
import umc.fitme.global.apiPayload.exception.ProjectException;

import java.util.LinkedHashMap;
import java.util.Map;

/***
 * ResponseEntityExceptionHandler를 상속해 스프링 MVC 표준 예외(405, 415, 깨진 JSON 등)까지
 * 모두 ApiResponse 형식으로 내려준다.
 *
 * 상속하지 않고 @ExceptionHandler(Exception.class)만 두면 ExceptionHandlerExceptionResolver가
 * DefaultHandlerExceptionResolver보다 먼저 동작하면서 표준 예외를 전부 가로채 500으로 만든다.
 *
 * 주의: 부모의 handleException(...)에 이미 매핑된 예외 타입(MethodArgumentNotValidException,
 * MissingServletRequestParameterException 등)은 @ExceptionHandler로 다시 선언하면
 * "Ambiguous @ExceptionHandler method mapped" 로 기동에 실패한다. 반드시 protected 메서드를
 * 오버라이드해야 한다.
 */
@RestControllerAdvice
@Slf4j
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    // 프로젝트에서 발생한 예외 처리
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectException(ProjectException e){
        BaseErrorCode errorCode = e.getErrorCode();
        log.warn("비즈니스 로직 예외: code={}, msg={}", errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }

    // @RequestParam enum 바인딩 실패 예외 처리 (예: category=FOO, sort=FOO)
    // MethodArgumentTypeMismatchException은 부모가 매핑한 TypeMismatchException의 하위 타입이라
    // 별도 @ExceptionHandler 선언이 가능하며, 더 구체적이므로 이쪽이 우선한다.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e){
        BaseErrorCode errorCode = resolveTypeMismatchErrorCode(e);
        log.warn("요청 파라미터 ENUM 바인딩 실패 예외: code={}, msg={}", errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }

    /***
     * 바인딩에 실패한 타입이 스스로 에러 코드를 정하게 위임한다.
     * 도메인 enum을 여기서 import 해 분기하면 global -> domain 역방향 의존이 생기고,
     * enum이 추가될 때마다 이 핸들러를 고쳐야 한다.
     */
    private BaseErrorCode resolveTypeMismatchErrorCode(MethodArgumentTypeMismatchException e) {
        Class<?> requiredType = e.getRequiredType();

        if (requiredType != null && InvalidValueErrorCodeProvider.class.isAssignableFrom(requiredType)) {
            Object[] constants = requiredType.getEnumConstants();
            if (constants != null && constants.length > 0) {
                return ((InvalidValueErrorCodeProvider) constants[0]).invalidValueErrorCode();
            }
        }

        return GeneralErrorCode.BAD_REQUEST;
    }

    // 낙관적 락 충돌 예외 처리 (동시 수정으로 커밋이 실패한 경우 409 로 재시도 유도)
    // 어떤 엔티티에서 충돌했는지는 여기서 알 수 없으므로 도메인 중립적인 코드를 쓴다.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailureException(OptimisticLockingFailureException e){
        BaseErrorCode errorCode = GeneralErrorCode.CONFLICT;
        log.warn("낙관적 락 충돌 예외: code={}, msg={}", errorCode.getCode(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }

    // @Valid 어노테이션 검증 실패 예외 처리
    // 부모가 이미 매핑한 타입이므로 @ExceptionHandler가 아닌 오버라이드로 처리한다.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // 검증 실패한 변수명과 실패 이유를 담을 Map
        // - LinkedHashMap: 한 필드에 제약이 여러 개 걸릴 수 있어 순서를 고정한다.
        // - putIfAbsent: put이면 뒤 제약이 앞 제약을 덮어써 어떤 메시지가 남을지 예측할 수 없다.
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach((error) ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        // 클래스 레벨 제약(@AssertTrue 등)의 메시지는 필드가 아닌 글로벌 에러로 잡히므로
        // 함께 담지 않으면 응답에서 통째로 사라진다.
        e.getBindingResult().getGlobalErrors().forEach((error) ->
                errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        return handleExceptionInternal(
                e,
                ApiResponse.onFailure(GeneralErrorCode.BAD_REQUEST, errors),
                headers,
                status,
                request);
    }

    /***
     * 부모가 처리하는 모든 MVC 표준 예외의 응답 본문을 ApiResponse로 통일한다.
     *
     * 부모의 일부 핸들러(handleHttpMessageNotReadable, handleTypeMismatch 등)는 RFC 9457
     * ProblemDetail을 본문으로 채워서 넘기므로, 이미 ApiResponse인 경우(이 클래스의 오버라이드가
     * 만든 검증 실패 필드 목록 등)만 보존하고 나머지는 교체해야 응답 포맷이 유지된다.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        BaseErrorCode errorCode = resolveByStatus(statusCode);

        if (statusCode.is5xxServerError()) {
            log.error("MVC 표준 예외(서버): code={}", errorCode.getCode(), e);
        } else {
            log.warn("MVC 표준 예외: code={}, msg={}", errorCode.getCode(), e.getMessage());
        }

        Object responseBody = (body instanceof ApiResponse<?>) ? body : ApiResponse.onFailure(errorCode, null);

        return super.handleExceptionInternal(e, responseBody, headers, statusCode, request);
    }

    // HTTP 상태를 대응하는 공통 에러 코드로 변환한다.
    private BaseErrorCode resolveByStatus(HttpStatusCode statusCode) {
        if (statusCode.equals(HttpStatus.BAD_REQUEST)) {
            return GeneralErrorCode.BAD_REQUEST;
        }
        if (statusCode.equals(HttpStatus.UNAUTHORIZED)) {
            return GeneralErrorCode.UNAUTHORIZED;
        }
        if (statusCode.equals(HttpStatus.FORBIDDEN)) {
            return GeneralErrorCode.FORBIDDEN;
        }
        if (statusCode.equals(HttpStatus.NOT_FOUND)) {
            return GeneralErrorCode.NOT_FOUND;
        }
        if (statusCode.equals(HttpStatus.METHOD_NOT_ALLOWED)) {
            return GeneralErrorCode.METHOD_NOT_ALLOWED;
        }
        if (statusCode.equals(HttpStatus.NOT_ACCEPTABLE)) {
            return GeneralErrorCode.NOT_ACCEPTABLE;
        }
        if (statusCode.equals(HttpStatus.PAYLOAD_TOO_LARGE)) {
            return GeneralErrorCode.PAYLOAD_TOO_LARGE;
        }
        if (statusCode.equals(HttpStatus.UNSUPPORTED_MEDIA_TYPE)) {
            return GeneralErrorCode.UNSUPPORTED_MEDIA_TYPE;
        }
        if (statusCode.equals(HttpStatus.SERVICE_UNAVAILABLE)) {
            return GeneralErrorCode.SERVICE_UNAVAILABLE;
        }

        // 위에서 다루지 않은 상태는 계열로만 구분한다.
        return statusCode.is4xxClientError()
                ? GeneralErrorCode.BAD_REQUEST
                : GeneralErrorCode.INTERNAL_SERVER_ERROR;
    }

    // 그 외 정의되지 않은 모든 예외 처리
    // 부모의 final handleException(Exception, WebRequest)와 이름이 겹치지 않도록 별도 이름을 쓴다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e){
        log.error("정의되지 않은 예외 발생", e);
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }
}
