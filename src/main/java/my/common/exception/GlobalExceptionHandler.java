package my.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import my.common.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 모든 커스텀 예외 처리 (ApplicationException 상속한 예외들)
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<?>> handleApplicationException(ApplicationException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("ApplicationException: {}", errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getStatus(), errorCode.getMessage()));
    }


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


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolation: {}", e.getMessage());

        return ResponseEntity
                .status(409)
                .body(ApiResponse.error(409, "데이터 무결성 제약조건 위반입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unexpected error: ", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.serverError("서버 오류가 발생했습니다."));
    }
}
