# BookShare 프로젝트 컨텍스트

## 프로젝트 개요
공유서점 REST API - Spring Boot 3.2.0 / Java 17 / Maven / MyBatis + Oracle 21c XE / JWT + AOP(@RequireRole)

## DB 접속 정보
```
DB_URL=jdbc:oracle:thin:@127.0.0.1:1521:XE
DB_USERNAME=system
DB_PASSWORD=Paperplane!23
JWT_SECRET=thisissecretkey991209bookshare1234567890
```
- Admin: `admin@bookshare.com` / `admin1234`

### sqlplus (WSL)
```bash
cmd.exe /c "set NLS_LANG=AMERICAN_AMERICA.AL32UTF8&& sqlplus -S system/\"Paperplane!23\"@127.0.0.1:1521/XE @C:\Users\tit\파일명.sql"
```
- SQL 파일은 Windows 경로(`C:\Users\tit\`)에 위치 필수. Linux 경로 불가
- 한글 주석 → 인코딩 깨짐. 한글 데이터는 UNISTR 사용, 주석은 영문으로
- UNISTR + CASE WHEN 비교 시 ORA-12704 발생 가능 → 한글 비교는 Java 레벨에서 처리

## 프로젝트 구조
```
src/main/java/my/
├── api/           # REST Controller
├── domain/        # Service(interface+impl) + Mapper + VO + DTO
├── annotation/    # @RequireRole, @ValidSettlementRatio
├── aop/           # RoleCheckAspect
├── common/        # ApiResponse, ErrorCode, 예외, BankCodeResolver
├── enums/         # Role(ADMIN,BOOK_OWNER,CUSTOMER,EMPLOYEE), BookState
├── filter/        # JwtFilter
├── jwt/           # JwtProvider
└── validator/
src/main/resources/mybatis/mapper/  # MyBatis XML
```

## 핵심 코딩 규칙

### INSERT: selectKey + CURRVAL 패턴
```xml
<insert id="insert" parameterType="...VO">
    <selectKey keyProperty="id" resultType="long" order="AFTER">
        SELECT "ISEQ$$_XXXXX".CURRVAL FROM DUAL
    </selectKey>
    INSERT INTO 테이블 (컬럼들) VALUES (값들)
</insert>
```
- useGeneratedKeys 사용 안 함. 시퀀스 매핑 표는 `docs/개발_레퍼런스.md` 참조
- BOOK_SALE_RECORD만 예외: BOOK.ID를 그대로 사용 (1:1 관계)

### MyBatis TIMESTAMP NULL 처리
- NULL 가능한 TIMESTAMP 컬럼은 반드시 `jdbcType=TIMESTAMP` 명시 (예: `#{suspendedAt,jdbcType=TIMESTAMP}`)
- 없으면 Oracle이 타입 추론 실패하여 오류 발생

### VO / DTO 패턴
- **VO**: DB 매핑 (MyBatis resultMap) + API 응답용. **DTO**: API 요청 전용 + 유효성 검증
- VO를 @RequestBody로 직접 받지 않음 (반드시 DTO 사용)

### Soft Delete + Book 상태 전이
- USERS/BOOK: DELETED_AT 컬럼 (NULL=활성)
- Book: `NORMAL → SOLD` (판매) / `NORMAL → SHOULD_BE_RETRIEVED` (임대 종료) / `SHOULD_BE_RETRIEVED → soft delete` (회수)

### COMMON_CODE 참조
- INSERT 시 `setGroupCodeId("LOCATION"/"BOOK_TYPE"/"BUY_TYPE")` 필수
- 코드 데이터 상세는 `docs/개발_레퍼런스.md` 참조

### 정산 핵심 정책
- 정산 실행(POST /settlements) 시에만 Settlement 생성 (settledAt 즉시 설정)
- 미정산 = BOOK_SALE_RECORD에서 BOOK_OWNER_SETTLEMENT_ID IS NULL
- 정산 시 ownerAmount 전액 송금 (임대료 공제는 보증금 스케줄러가 담당)
- 상세 검증 순서/송금 흐름은 `docs/개발_레퍼런스.md` 참조

### 보증금/임대료
- DEPOSIT: BOOK_OWNER 1:1 (책소유주 단위 통합 보증금)
- 점유(occupy) 시 만기일까지 월별 RENTAL_SETTLEMENT 일괄 생성 (입거월은 일할 계산)
- 연체 공제 스케줄러: 매월 1일 05:00 (FIFO 공제 → SUSPENDED → 강제 퇴거)
- 정산 스케줄러: 매월 1일 06:00

## 테스트
```bash
cd "/mnt/c/Users/tit/OneDrive/바탕 화면/bookshare" && cmd.exe /c "C:\Users\tit\run_test.cmd" 2>&1 | tail -10
```
- 현재 테스트 수: **208개** (기존 156 + 신규 52)
- 실패 확인: `| grep -E "Tests run:|FAIL|ERROR.*Test" | head -30`

### 테스트 작성 패턴
```java
@SpringBootTest
@Transactional
class XxxTest {
    // @Autowired 통합 테스트 / uniqueCode() / @DisplayName 한글 / AssertJ
}
```
- 데이터 생성 순서: 정산비율 → 책장타입 → 책장 → BookOwner(계좌) → 점유 → 책등록 → 판매
- **중복 방지**: phone, email, residentNumber, BookCaseType code → `uniqueCode()` 필수
- **bankName**: `"국민은행"` 등 BankCodeResolver 등록 한글명 사용 (`"KB"` 금지)
- **코드값**: locationCode=`"01"`, bookTypeCode=`"04"`, buyTypeCommonCode=`"01"` (이름 아닌 코드)
- soft delete 검증: `bookMapper.selectByIdIncludeDeleted(id)`

## 개발 진행 현황

### 완료: Phase A — 고객 페이지 확장 (프로필/포스트/댓글/팔로우)
기존 관리자·책소유주 중심이던 서비스에 **고객 대상 소셜 기능** 4개 도메인을 추가했다.

| 도메인 | 테이블 | 시퀀스 | 핵심 기능 |
|---|---|---|---|
| **BookOwnerProfile** | BOOK_OWNER_PROFILE | 없음 (PK=FK) | 닉네임(UNIQUE), 좋아하는 책/작가/장르 |
| **BookOwnerPost** | BOOK_OWNER_POST | ISEQ$$_78832 | 인스타 피드형 게시글, 책 연결(선택), soft delete |
| **PostComment** | POST_COMMENT | ISEQ$$_78835 | 댓글+1단계 대댓글, 모든 사용자 작성 가능, soft delete |
| **Follow** | FOLLOW | ISEQ$$_78838 | 고객→BookOwner 팔로우, 언팔로우(hard delete), 피드 조회 |

- 각 도메인: VO + DTO + Mapper(XML) + Service(interface+impl) + Controller
- 테스트: 5개 파일 52개 테스트 (프로필 11, 포스트 12, 댓글 10, 팔로우 12, 피드 7)
- ERD: `dbdiagram.dbml`에 4개 테이블 추가 완료

### 다음 단계
1. **Phase B: 고객 QnA 게시판** — 고객 질문 + 관리자 답변 구조 (미착수)
2. **docs/개발_레퍼런스.md 업데이트** — 신규 시퀀스 매핑 3건 추가 필요
3. **RoleCheckAspect 확장 검토** — 현재 checkOwnership이 BOOK_OWNER만 지원, CUSTOMER 지원 필요 여부 검토

## 참조 문서
> **아래 문서는 해당 도메인 작업 시에만 읽을 것. 요청 전까지 읽지 말 것.**

| 문서 | 내용 |
|---|---|
| `docs/개발_레퍼런스.md` | 시퀀스 매핑, 정산 상세, 토스 연동, COMMON_CODE, Unique Index, 테이블 스키마 |
| `설계_및_정책.md` | 임대료/보증금/연체 정책 상세, 테이블 설계 |
| `BOOKSHARE_개발순서_및_서비스플로우.md` | 서비스 플로우 및 체크 로직 |
