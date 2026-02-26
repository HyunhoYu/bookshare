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
    DUPLICATE_PHONE("이미 존재하는 전화번호입니다", 409),
    DUPLICATE_RESIDENT_NUMBER("이미 존재하는 주민번호입니다", 409),
    FORBIDDEN("접근 권한이 없습니다", 403),
    BOOK_CASE_TYPE_INSERT_FAIL("책장 타입 저장 실패", 500),
    BOOK_CASE_TYPE_NOT_FOUND("존재하지 않는 책장 타입입니다", 404),
    DUPLICATE_BOOK_CASE_TYPE_CODE("이미 존재하는 책장 타입 코드입니다", 409),
    BOOK_CASE_INSERT_FAIL("책장 저장 실패", 500),
    BOOK_CASE_NOT_FOUND("존재하지 않는 책장입니다", 404),
    BOOK_CASE_ALREADY_OCCUPIED("이미 점유 중인 책장입니다", 409),
    BOOK_OWNER_MISMATCH("일괄 등록 시 모든 책의 소유주가 동일해야 합니다", 400),
    BOOK_OWNER_NOT_FOUND("존재하지 않는 책 소유주입니다", 404),
    BOOK_CASE_NOT_OCCUPIED_BY_OWNER("해당 책장을 점유 중인 소유주가 아닙니다", 403),
    INVALID_BOOK_TYPE("존재하지 않는 책 분류입니다", 400),
    INVALID_LOCATION_CODE("존재하지 않는 위치 코드입니다", 400),
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
    CUSTOMER_INSERT_FAIL("고객 저장 실패", 500),
    ADDRESS_INSERT_FAIL("주소 저장 실패", 500),
    SETTLEMENT_AMOUNT_ZERO("정산 금액이 0원입니다", 400),
    BANK_CODE_NOT_FOUND("은행 코드를 찾을 수 없습니다", 400),
    INVALID_BUY_TYPE("존재하지 않는 구매 유형 코드입니다", 400),
    RENTAL_SETTLEMENT_INSERT_FAIL("임대료 정산 레코드 생성 실패", 500),
    RENTAL_SETTLEMENT_NOT_FOUND("존재하지 않는 임대료 정산 레코드입니다", 404),
    RENTAL_SETTLEMENT_ALREADY_PAID("이미 납부된 임대료입니다", 409),
    UNSETTLED_SALE_RECORD_EXISTS("미정산 판매기록이 존재하여 임대 종료할 수 없습니다", 400),
    DEPOSIT_INSERT_FAIL("보증금 저장 실패", 500),

    // Profile
    PROFILE_ALREADY_EXISTS("이미 프로필이 존재합니다", 409),
    PROFILE_NOT_FOUND("프로필이 존재하지 않습니다", 404),
    PROFILE_INSERT_FAIL("프로필 저장 실패", 500),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다", 409),

    // Post
    POST_NOT_FOUND("존재하지 않는 게시글입니다", 404),
    POST_INSERT_FAIL("게시글 저장 실패", 500),
    POST_BOOK_OWNER_MISMATCH("본인의 책만 게시글에 연결할 수 있습니다", 400),

    // Comment
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다", 404),
    COMMENT_INSERT_FAIL("댓글 저장 실패", 500),
    COMMENT_NOT_REPLY_TARGET("대댓글에는 답글을 달 수 없습니다", 400),

    // Follow
    ALREADY_FOLLOWING("이미 팔로우 중입니다", 409),
    NOT_FOLLOWING("팔로우하지 않은 책소유주입니다", 400),
    FOLLOW_INSERT_FAIL("팔로우 저장 실패", 500),
    ;

    private final String message;
    private final int status;
}
