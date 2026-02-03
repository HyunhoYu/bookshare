package my.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("등록되지 않은 이메일", 404),
    INCORRECT_PASSWORD("패스워드 불일치", 401),
    JWT_PARSE_FAIL("토큰 파싱 실패", 401),
    USER_INSERT_FAIL("유저 저장 실패", 500),
    DUPLICATE_EMAIL("이미 존재하는 이메일입니다", 409),
    FORBIDDEN("접근 권한이 없습니다", 403),
    ;

    private final String message;
    private final int status;
}
