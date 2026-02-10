# BookShare 개발 순서 및 서비스 플로우

> 작성일: 2026-02-05

---

## 1. API 개발 순서

### Phase 1: 시스템 초기 설정
| 순서 | Method | Endpoint | 설명 | 비고 |
|-----|--------|----------|------|------|
| 1 | PUT | `/settings/settlement-ratio` | 정산 비율 설정 | 판매 전 필수 |
| 2 | POST | `/book-case-types` | 책장 타입 등록 | 가격 포함 |
| 3 | POST | `/book-cases` | 책장 등록 | 타입 지정 |

### Phase 2: BookOwner 등록 및 책장 점유
| 순서 | Method | Endpoint | 설명 | 비고 |
|-----|--------|----------|------|------|
| 4 | POST | `/book-owners` | 책소유주 등록 | 계좌정보 포함 |
| 5 | POST | `/book-cases/{id}/occupy` | 책장 점유 시작 | 점유기록 자동생성 |

### Phase 3: 책 등록 및 판매
| 순서 | Method | Endpoint | 설명 | 비고 |
|-----|--------|----------|------|------|
| 6 | POST | `/books` | 책 등록 | 본인 점유 책장만 |
| 7 | POST | `/sales` | 책 판매 | sale_record 생성 |

### Phase 4: 정산
| 순서 | Method | Endpoint | 설명 | 비고 |
|-----|--------|----------|------|------|
| 8 | GET | `/settlements/pending` | 미정산 내역 조회 | Admin용 전체 |
| 9 | GET | `/book-owners/{id}/settlements/pending` | BookOwner별 미정산 | 본인 조회 |
| 10 | POST | `/settlements` | 정산 실행 | 배치 정산 가능 |

### Phase 5: 책장 임대 종료 및 회수
| 순서 | Method | Endpoint | 설명 | 비고 |
|-----|--------|----------|------|------|
| 11 | POST | `/book-cases/{id}/release` | 책장 임대 종료 | 트랜잭션 처리 |
| 12 | GET | `/books/should-retrieve` | 회수 대기 목록 | |
| 13 | POST | `/books/{id}/retrieve` | 책 회수 완료 | 단건 |
| 14 | POST | `/books/retrieve-batch` | 책 일괄 회수 | 다건 |
~~~~
### Phase 6: 조회 API (기구현 + 추가)
| 순서 | Method | Endpoint | 설명 | 상태 |
|-----|--------|----------|------|------|
| - | GET | `/book-owners` | 책소유주 목록 | 구현완료 |
| - | GET | `/book-owners/{id}` | 책소유주 상세 | 구현완료 |
| - | GET | `/book-owners/{id}/books` | 본인 책 목록 | 구현완료 |
| - | GET | `/book-owners/{id}/books/sold` | 판매된 책 목록 | 구현완료 |
| - | GET | `/book-owners/{id}/settlements` | 전체 정산 내역 | 구현완료 |
| - | GET | `/book-owners/{id}/settlements/completed` | 정산 완료 내역 | 구현완료 |
| - | GET | `/book-owners/{id}/settlements/pending` | 미정산 내역 | 구현완료 |

---

## 2. 주요 서비스 플로우

### 전체 플로우 다이어그램

```
[시스템 초기 설정]
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  1. 정산 비율 설정 (PUT /settings/settlement-ratio)          │
│  2. 책장 타입 등록 (POST /book-case-types)                   │
│  3. 책장 등록 (POST /book-cases)                             │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
[BookOwner 라이프사이클 시작]
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  4. BookOwner 등록 (POST /book-owners)                       │
│     - users 테이블 + book_owner 테이블 + bank_account 생성   │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  5. 책장 점유 시작 (POST /book-cases/{id}/occupy)            │
│     ✓ 체크: 해당 책장이 이미 점유 중인지                      │
│       (un_occupied_at IS NULL인 레코드 있으면 불가)          │
│     → book_case_occupied_record 생성                        │
│     → book_case.current_book_owner_id 업데이트              │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  6. 책 등록 (POST /books)                                    │
│     ✓ 체크: 본인이 점유 중인 책장인지                         │
│     → book 테이블에 INSERT (state = 'NORMAL')               │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
[판매 사이클] ─────────────────────────────────────────────────
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  7. 책 판매 (POST /sales)                                    │
│     ✓ 체크: 책 상태가 NORMAL인지                             │
│     → book.state = 'SOLD'                                   │
│     → book_sale_record 생성 (settlement_id = NULL, 미정산)  │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  8. 미정산 내역 조회                                         │
│     - Admin: GET /settlements/pending                       │
│     - BookOwner: GET /book-owners/{id}/settlements/pending  │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  9. 정산 실행 (POST /settlements)                            │
│     ✓ 체크: saleRecordIds가 모두 해당 bookOwner 소유인지     │
│     ✓ 체크: 이미 정산된 판매기록이 아닌지                     │
│     ✓ 체크: bookOwner 계좌 정보 등록되어 있는지              │
│     → book_owner_settlement 생성                            │
│     → 해당 sale_record들의 settlement_id 업데이트           │
│                                                             │
│     ※ 배치 정산 가능 (여러 판매 → 1건의 정산)                │
└─────────────────────────────────────────────────────────────┘
     │
     │ (추가 판매 발생 가능 - 7번으로 반복)
     │
     ▼
[책장 임대 종료] ──────────────────────────────────────────────
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  10. 책장 임대 종료 (POST /book-cases/{id}/release)          │
│      ※ 하나의 트랜잭션으로 처리                              │
│                                                             │
│      → book_case_occupied_record.un_occupied_at 설정        │
│      → book_case.current_book_owner_id = NULL               │
│      → 해당 책장의 NORMAL 상태 책들:                         │
│        - book.state = 'SHOULD_BE_RETRIEVED'                 │
│        - book_should_be_retrieve 테이블에 INSERT            │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
[회수 및 마무리 정산] ─────────────────────────────────────────
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  11. 회수 대기 목록 조회 (GET /books/should-retrieve)        │
│      - bookOwnerId 파라미터로 필터링 가능                    │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  12. 미정산 내역 확인 후 정산                                 │
│      - GET /book-owners/{id}/settlements/pending            │
│      - 미정산 있으면 → POST /settlements                     │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  13. 책 회수 완료 (POST /books/{id}/retrieve)                │
│      ✓ 체크: state가 SHOULD_BE_RETRIEVED인지                 │
│      → book_should_be_retrieve에서 DELETE                   │
│      → book 레코드는 유지 (기록 보존)                        │
│                                                             │
│      또는 일괄 회수: POST /books/retrieve-batch              │
└─────────────────────────────────────────────────────────────┘
     │
     ▼
[새로운 BookOwner 점유] ───────────────────────────────────────
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  14. 새 BookOwner가 같은 책장 점유                            │
│      (POST /book-cases/{id}/occupy)                         │
│                                                             │
│      ✓ 체크: un_occupied_at IS NULL인 레코드 없어야 함       │
│        (같은 책장에 2명 이상 점유 불가)                       │
│      → 새로운 book_case_occupied_record 생성                │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 핵심 체크 로직 정리

### 책장 점유 시작 (occupy)
```sql
-- 이미 점유 중인지 체크
SELECT COUNT(*) FROM book_case_occupied_record
WHERE book_case_id = ? AND un_occupied_at IS NULL;
-- 결과가 0이어야 점유 가능
```

### 책 등록 시
```sql
-- 본인이 점유 중인 책장인지 체크
SELECT COUNT(*) FROM book_case_occupied_record
WHERE book_case_id = ?
  AND book_owner_id = ?
  AND un_occupied_at IS NULL;
-- 결과가 1이어야 등록 가능
```

### 책 판매 시
```sql
-- 책 상태가 NORMAL인지 체크
SELECT state FROM book WHERE id = ?;
-- state = 'NORMAL' 이어야 판매 가능
```

### 정산 실행 시
```sql
-- 1. 모든 saleRecordIds가 해당 bookOwner 소유인지
SELECT COUNT(*) FROM book_sale_record sr
JOIN book b ON sr.id = b.id
WHERE sr.id IN (?, ?, ?) AND b.book_owner_id = ?;

-- 2. 이미 정산된 기록이 아닌지
SELECT COUNT(*) FROM book_sale_record
WHERE id IN (?, ?, ?) AND book_owner_settlement_id IS NOT NULL;
-- 결과가 0이어야 정산 가능

-- 3. 계좌 정보 존재 여부
SELECT COUNT(*) FROM bank_account WHERE book_owner_id = ?;
```

---

## 4. 트랜잭션 처리 필수 API

| API | 트랜잭션 내 처리 항목 |
|-----|---------------------|
| `POST /book-owners` | users INSERT + book_owner INSERT + bank_account INSERT |
| `POST /book-cases/{id}/occupy` | occupied_record INSERT + book_case UPDATE |
| `POST /sales` | book UPDATE (state) + sale_record INSERT |
| `POST /settlements` | settlement INSERT + sale_record UPDATE (다건) |
| `POST /book-cases/{id}/release` | occupied_record UPDATE + book UPDATE (다건) + should_retrieve INSERT (다건) |

---

## 5. 상태 전이도

### Book.state
```
NORMAL ──[판매]──→ SOLD
   │
   └──[임대종료]──→ SHOULD_BE_RETRIEVED ──[회수완료]──→ (레코드 유지, should_retrieve에서만 삭제)
```

### 정산 상태 (book_sale_record 기준)
```
[판매] → settlement_id = NULL (미정산)
            │
            └──[정산실행]──→ settlement_id = {값} (정산완료)
```

---

## 6. 개발 우선순위 요약

```
1순위: Phase 1~3 (시스템 설정 + 책장)
2순위: Phase 4~5 (BookOwner + 점유)
3순위: Phase 6~7 (책 등록 + 판매)
4순위: Phase 8~10 (정산)
5순위: Phase 11~14 (임대종료 + 회수)
```

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-02-05 | 최초 작성 |
