package my.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import my.common.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<?>> handleApplicationException(ApplicationException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("ApplicationException: {}", errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getStatus(), errorCode.getMessage()));
    }

    // @Valid 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("Validation failed: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(message));
    }

    // @Validated 제약조건 위반 (PathVariable, RequestParam 등)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("입력값 제약조건을 위반했습니다.");

        log.warn("ConstraintViolation: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(message));
    }

    // 잘못된 JSON 요청 본문
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadable: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("요청 본문의 형식이 올바르지 않습니다."));
    }

    // 필수 쿼리 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("MissingParam: {}", e.getParameterName());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("필수 파라미터가 누락되었습니다: " + e.getParameterName()));
    }

    // 타입 불일치 (PathVariable에 문자열 대신 숫자 등)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("TypeMismatch: {} = {}", e.getName(), e.getValue());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("파라미터 '" + e.getName() + "'의 타입이 올바르지 않습니다."));
    }

    // DB 무결성 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolation: {}", e.getMessage());

        return ResponseEntity
                .status(409)
                .body(ApiResponse.error(409, "데이터 무결성 제약조건 위반입니다."));
    }

    // 기타 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unexpected error: ", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.serverError("서버 오류가 발생했습니다."));
    }
}
