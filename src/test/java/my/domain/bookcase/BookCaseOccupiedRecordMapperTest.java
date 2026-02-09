package my.domain.bookcase;

import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeVO;
import my.domain.bookcasetype.service.BookCaseTypeService;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookCaseOccupiedRecordMapperTest {

    @Autowired
    private BookCaseOccupiedRecordMapper occupiedRecordMapper;

    @Autowired
    private BookCaseService bookCaseService;

    @Autowired
    private BookCaseTypeService bookCaseTypeService;

    @Autowired
    private BookOwnerAuthService bookOwnerAuthService;

    private long bookCaseId;
    private long bookOwnerId;

    @BeforeEach
    void setUp() {
        // 책장 타입 생성
        BookCaseTypeVO typeVO = new BookCaseTypeVO();
        typeVO.setCode("T1");
        typeVO.setMonthlyPrice(30000);
        long typeId = bookCaseTypeService.addBookCaseType(typeVO);

        // 책장 생성
        BookCaseVO caseVO = new BookCaseVO();
        caseVO.setLocationCode("OCC-TEST-01");
        caseVO.setBookCaseTypeId(typeId);
        bookCaseId = bookCaseService.addBookCase(caseVO);

        // BookOwner 생성
        BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                .name("점유테스트")
                .email("occupy-test@test.com")
                .phone("010-9999-8888")
                .password("test1234")
                .residentNumber("880101-1234567")
                .bankName("신한은행")
                .accountNumber("111-222-333")
                .build();
        BookOwnerVO owner = bookOwnerAuthService.signup(dto);
        bookOwnerId = owner.getId();
    }

    @Test
    @DisplayName("점유 기록 삽입 - ID 자동 채번")
    void insert_success() {
        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookCaseId(bookCaseId);
        record.setBookOwnerId(bookOwnerId);

        int result = occupiedRecordMapper.insert(record);

        assertThat(result).isEqualTo(1);
        assertThat(record.getId()).isNotNull().isGreaterThan(0);
    }

    @Test
    @DisplayName("현재 점유 중인 레코드 조회 - un_occupied_at이 NULL인 것만")
    void selectCurrentByBookCaseId_returnsOccupied() {
        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookCaseId(bookCaseId);
        record.setBookOwnerId(bookOwnerId);
        occupiedRecordMapper.insert(record);

        BookCaseOccupiedRecordVO current = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);

        assertThat(current).isNotNull();
        assertThat(current.getBookCaseId()).isEqualTo(bookCaseId);
        assertThat(current.getBookOwnerId()).isEqualTo(bookOwnerId);
        assertThat(current.getOccupiedAt()).isNotNull();
        assertThat(current.getUnOccupiedAt()).isNull();
    }

    @Test
    @DisplayName("점유 기록 없는 책장 조회 시 null 반환")
    void selectCurrentByBookCaseId_noRecord_returnsNull() {
        BookCaseOccupiedRecordVO current = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
        assertThat(current).isNull();
    }

    @Test
    @DisplayName("점유 해제 후 현재 점유자 조회 시 null 반환")
    void updateUnOccupiedAt_thenSelectCurrent_returnsNull() {
        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookCaseId(bookCaseId);
        record.setBookOwnerId(bookOwnerId);
        occupiedRecordMapper.insert(record);

        // 점유 해제
        int updated = occupiedRecordMapper.updateUnOccupiedAt(record.getId());
        assertThat(updated).isEqualTo(1);

        // 현재 점유자 없어야 함
        BookCaseOccupiedRecordVO current = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
        assertThat(current).isNull();
    }

    @Test
    @DisplayName("BookOwner별 점유 이력 조회 - 최신순 정렬")
    void selectByBookOwnerId_returnsHistory() {
        // 책장 2개에 점유 기록 생성
        BookCaseTypeVO typeVO2 = new BookCaseTypeVO();
        typeVO2.setCode("T2");
        typeVO2.setMonthlyPrice(40000);
        long typeId2 = bookCaseTypeService.addBookCaseType(typeVO2);

        BookCaseVO case2 = new BookCaseVO();
        case2.setLocationCode("OCC-TEST-02");
        case2.setBookCaseTypeId(typeId2);
        long bookCaseId2 = bookCaseService.addBookCase(case2);

        BookCaseOccupiedRecordVO r1 = new BookCaseOccupiedRecordVO();
        r1.setBookCaseId(bookCaseId);
        r1.setBookOwnerId(bookOwnerId);
        occupiedRecordMapper.insert(r1);

        BookCaseOccupiedRecordVO r2 = new BookCaseOccupiedRecordVO();
        r2.setBookCaseId(bookCaseId2);
        r2.setBookOwnerId(bookOwnerId);
        occupiedRecordMapper.insert(r2);

        List<BookCaseOccupiedRecordVO> history = occupiedRecordMapper.selectByBookOwnerId(bookOwnerId);

        assertThat(history).hasSize(2);
        assertThat(history).allMatch(r -> r.getBookOwnerId().equals(bookOwnerId));
    }

    @Test
    @DisplayName("점유 → 해제 → 재점유 시나리오")
    void occupy_release_reoccupy() {
        // 1. 점유
        BookCaseOccupiedRecordVO r1 = new BookCaseOccupiedRecordVO();
        r1.setBookCaseId(bookCaseId);
        r1.setBookOwnerId(bookOwnerId);
        occupiedRecordMapper.insert(r1);

        // 2. 해제
        occupiedRecordMapper.updateUnOccupiedAt(r1.getId());

        // 3. 다른 사람이 재점유
        BookOwnerVO owner2 = bookOwnerAuthService.signup(
                BookOwnerJoinRequestDto.builder()
                        .name("재점유자")
                        .email("reoccupy@test.com")
                        .phone("010-1111-2222")
                        .password("pass1234")
                        .residentNumber("950202-2345678")
                        .bankName("우리은행")
                        .accountNumber("444-555-666")
                        .build()
        );

        BookCaseOccupiedRecordVO r2 = new BookCaseOccupiedRecordVO();
        r2.setBookCaseId(bookCaseId);
        r2.setBookOwnerId(owner2.getId());
        occupiedRecordMapper.insert(r2);

        // 현재 점유자는 owner2
        BookCaseOccupiedRecordVO current = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
        assertThat(current).isNotNull();
        assertThat(current.getBookOwnerId()).isEqualTo(owner2.getId());
    }
}
