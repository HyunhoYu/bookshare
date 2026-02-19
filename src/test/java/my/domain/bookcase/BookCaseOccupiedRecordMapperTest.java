package my.domain.bookcase;

import my.domain.bookcase.service.BookCaseService;
import my.domain.bookcasetype.BookCaseTypeCreateDto;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        String code = uniqueCode();

        // 책장 타입 생성
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(code);
        typeDto.setMonthlyPrice(30000);
        long typeId = bookCaseTypeService.create(typeDto);

        // 책장 생성
        BookCaseCreateDto caseDto = new BookCaseCreateDto();
        caseDto.setLocationCode("01");
        caseDto.setBookCaseTypeId(typeId);
        bookCaseId = bookCaseService.create(caseDto);

        // BookOwner 생성
        BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                .name("점유테스트" + code)
                .email("occupy-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("test1234")
                .residentNumber(code + "-1234567")
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
        record.setExpirationDate(LocalDate.now().plusMonths(3));
        record.setDeposit(30000);

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
        record.setExpirationDate(LocalDate.now().plusMonths(3));
        record.setDeposit(30000);
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
        record.setExpirationDate(LocalDate.now().plusMonths(3));
        record.setDeposit(30000);
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
        BookCaseTypeCreateDto typeDto2 = new BookCaseTypeCreateDto();
        typeDto2.setCode(uniqueCode());
        typeDto2.setMonthlyPrice(40000);
        long typeId2 = bookCaseTypeService.create(typeDto2);

        BookCaseCreateDto case2Dto = new BookCaseCreateDto();
        case2Dto.setLocationCode("02");
        case2Dto.setBookCaseTypeId(typeId2);
        long bookCaseId2 = bookCaseService.create(case2Dto);

        BookCaseOccupiedRecordVO r1 = new BookCaseOccupiedRecordVO();
        r1.setBookCaseId(bookCaseId);
        r1.setBookOwnerId(bookOwnerId);
        r1.setExpirationDate(LocalDate.now().plusMonths(3));
        r1.setDeposit(30000);
        occupiedRecordMapper.insert(r1);

        BookCaseOccupiedRecordVO r2 = new BookCaseOccupiedRecordVO();
        r2.setBookCaseId(bookCaseId2);
        r2.setBookOwnerId(bookOwnerId);
        r2.setExpirationDate(LocalDate.now().plusMonths(3));
        r2.setDeposit(40000);
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
        r1.setExpirationDate(LocalDate.now().plusMonths(3));
        r1.setDeposit(30000);
        occupiedRecordMapper.insert(r1);

        // 2. 해제
        occupiedRecordMapper.updateUnOccupiedAt(r1.getId());

        // 3. 다른 사람이 재점유
        String code2 = uniqueCode();
        BookOwnerVO owner2 = bookOwnerAuthService.signup(
                BookOwnerJoinRequestDto.builder()
                        .name("재점유자" + code2)
                        .email("reoccupy-" + code2 + "@test.com")
                        .phone("010-" + code2.substring(0, 4) + "-" + code2.substring(4))
                        .password("pass1234")
                        .residentNumber(code2 + "-2345678")
                        .bankName("우리은행")
                        .accountNumber("444-555-666")
                        .build()
        );

        BookCaseOccupiedRecordVO r2 = new BookCaseOccupiedRecordVO();
        r2.setBookCaseId(bookCaseId);
        r2.setBookOwnerId(owner2.getId());
        r2.setExpirationDate(LocalDate.now().plusMonths(3));
        r2.setDeposit(30000);
        occupiedRecordMapper.insert(r2);

        // 현재 점유자는 owner2
        BookCaseOccupiedRecordVO current = occupiedRecordMapper.selectCurrentByBookCaseId(bookCaseId);
        assertThat(current).isNotNull();
        assertThat(current.getBookOwnerId()).isEqualTo(owner2.getId());
    }
}
