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
    BOOK_OWNER_MISMATCH("일괄 등록 시 모든 책의 소유주가 동일해야 합니다", 400),
    BOOK_OWNER_NOT_FOUND("존재하지 않는 책 소유주입니다", 404),
    BOOK_CASE_NOT_OCCUPIED_BY_OWNER("해당 책장을 점유 중인 소유주가 아닙니다", 403),
    INVALID_BOOK_TYPE("존재하지 않는 책 분류입니다", 400),
    BOOK_INSERT_FAIL("책 저장 실패", 500),
    CUSTOMER_NOT_FOUND("존재하지 않는 고객입니다", 404),
    BOOK_NOT_FOUND("존재하지 않는 책입니다", 404),
    BOOK_SALE_RECORD_INSERT_FAIL("판매 기록 저장 실패", 500),
    EMPTY_BOOK_SALE_REQUEST("판매할 책 목록이 비어있습니다", 400),
    BOOK_ALREADY_SOLD("이미 판매된 책입니다", 409),
    SETTLEMENT_INSERT_FAIL("정산 레코드 생성 실패", 500),
    SETTLEMENT_NO_UNSETTLED("미정산 내역이 없습니다", 404),
    SETTLEMENT_TRANSFER_FAIL("정산 송금 실패", 500),
    BOOK_OWNER_BANK_ACCOUNT_NOT_FOUND("책 주인의 계좌 정보가 없습니다", 404),
    SETTLEMENT_SALE_RECORD_OWNER_MISMATCH("해당 책소유주의 판매기록이 아닙니다", 400),
    SETTLEMENT_ALREADY_SETTLED("이미 정산된 판매기록이 포함되어 있습니다", 409),
    EMPTY_SETTLEMENT_REQUEST("정산할 판매기록 목록이 비어있습니다", 400),
    EMPTY_RETRIEVE_REQUEST("회수할 책 목록이 비어있습니다", 400),
    BOOK_NOT_RETRIEVABLE("회수 대기 상태가 아닌 책이 포함되어 있습니다", 400),
    RETRIEVE_FAIL("책 회수 처리 실패", 500),
    EMPTY_UNOCCUPY_REQUEST("점유 해제할 책장 목록이 비어있습니다", 400),
    BOOK_CASE_NOT_OCCUPIED("점유 중이 아닌 책장입니다", 400),
    UNOCCUPY_FAIL("점유 해제 처리 실패", 500),
    ;

    private final String message;
    private final int status;
}
