package my.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import my.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UserNotFoundException.class)
    public ApiResponse<?> handleUserNotFound(UserNotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ApiResponse.error(errorCode.getStatus(), errorCode.getMessage());
    }


}
