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
    SETTLEMENT_RATIO_INSERT_FAIL("정산 비율 저장 실패", 500),
    DUPLICATE_EMAIL("이미 존재하는 이메일입니다", 409),
    FORBIDDEN("접근 권한이 없습니다", 403),
    BOOK_CASE_TYPE_INSERT_FAIL("책장 타입 저장 실패", 500),
    BOOK_CASE_TYPE_NOT_FOUND("존재하지 않는 책장 타입입니다", 404),
    BOOK_CASE_NOT_FOUND("존재하지 않는 책장입니다", 404),
    BOOK_CASE_ALREADY_OCCUPIED("이미 점유 중인 책장입니다", 409),
    ;

    private final String message;
    private final int status;
}
