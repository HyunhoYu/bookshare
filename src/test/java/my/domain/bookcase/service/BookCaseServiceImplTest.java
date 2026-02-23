package my.domain.bookcase.service;

import my.domain.bookcase.BookCaseCreateDto;
import my.domain.bookcase.BookCaseOccupiedRecordMapper;
import my.domain.bookcase.BookCaseOccupiedRecordVO;
import my.domain.bookcase.BookCaseVO;
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

import my.common.exception.ApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookCaseServiceImplTest {

    @Autowired
    private BookCaseService bookCaseService;

    @Autowired
    private BookCaseTypeService bookCaseTypeService;

    @Autowired
    private BookOwnerAuthService bookOwnerAuthService;

    @Autowired
    private BookCaseOccupiedRecordMapper occupiedRecordMapper;

    private long typeId;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        BookCaseTypeCreateDto typeDto = new BookCaseTypeCreateDto();
        typeDto.setCode(uniqueCode());
        typeDto.setMonthlyPrice(50000);
        typeId = bookCaseTypeService.create(typeDto);
    }

    private BookCaseCreateDto createDto(String locationCode, long bookCaseTypeId) {
        BookCaseCreateDto dto = new BookCaseCreateDto();
        dto.setLocationCode(locationCode);
        dto.setBookCaseTypeId(bookCaseTypeId);
        return dto;
    }

    private BookOwnerVO createBookOwner(String email) {
        String code = uniqueCode();
        BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                .name("테스트유저" + code)
                .email(email)
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build();
        return bookOwnerAuthService.signup(dto);
    }

    // === 기존 CRUD 테스트 ===

    @Test
    @DisplayName("책장 등록 성공 - ID 반환")
    void addBookCase_success() {
        BookCaseCreateDto dto = createDto("01", typeId);
        long id = bookCaseService.create(dto);
        assertThat(id).isGreaterThan(0);
    }

    @Test
    @DisplayName("책장 등록 후 ID로 조회 - 필드값 일치")
    void addBookCase_thenFindById() {
        BookCaseCreateDto dto = createDto("01", typeId);
        long id = bookCaseService.create(dto);

        BookCaseVO result = bookCaseService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCommonCodeId()).isEqualTo("01");
        assertThat(result.getBookCaseTypeId()).isEqualTo(typeId);
    }

    @Test
    @DisplayName("여러 책장 등록 후 전체 조회")
    void addMultiple_thenFindAll() {
        bookCaseService.create(createDto("01", typeId));
        bookCaseService.create(createDto("02", typeId));

        List<BookCaseVO> list = bookCaseService.findAll();

        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 book_case_type_id로 등록 시 예외 발생")
    void addBookCase_invalidTypeId_throwsException() {
        BookCaseCreateDto dto = createDto("01", 999999L);

        assertThatThrownBy(() -> bookCaseService.create(dto))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 null 반환")
    void findById_notFound() {
        BookCaseVO result = bookCaseService.findById(999999L);
        assertThat(result).isNull();
    }

    // === findUsableBookCases 테스트 ===

    @Test
    @DisplayName("책장 모두 비어있을 때 - 전부 이용 가능")
    void findUsableBookCases_allEmpty() {
        long id1 = bookCaseService.create(createDto("01", typeId));
        long id2 = bookCaseService.create(createDto("02", typeId));

        List<BookCaseVO> usable = bookCaseService.findUsable();

        assertThat(usable).extracting(BookCaseVO::getId).contains(id1, id2);
    }

    @Test
    @DisplayName("점유된 책장은 이용 가능 목록에서 제외")
    void findUsableBookCases_occupiedExcluded() {
        long caseId1 = bookCaseService.create(createDto("01", typeId));
        long caseId2 = bookCaseService.create(createDto("02", typeId));

        // caseId1을 점유
        BookOwnerVO owner = createBookOwner("usable-test@test.com");
        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookCaseId(caseId1);
        record.setBookOwnerId(owner.getId());
        record.setExpirationDate(LocalDate.now().plusMonths(3));
        occupiedRecordMapper.insert(record);

        List<BookCaseVO> usable = bookCaseService.findUsable();

        assertThat(usable).extracting(BookCaseVO::getId)
                .contains(caseId2)
                .doesNotContain(caseId1);
    }

    @Test
    @DisplayName("점유 해제된 책장은 다시 이용 가능")
    void findUsableBookCases_releasedIncluded() {
        long caseId = bookCaseService.create(createDto("01", typeId));

        // 점유
        BookOwnerVO owner = createBookOwner("release-test@test.com");
        BookCaseOccupiedRecordVO record = new BookCaseOccupiedRecordVO();
        record.setBookCaseId(caseId);
        record.setBookOwnerId(owner.getId());
        record.setExpirationDate(LocalDate.now().plusMonths(3));
        occupiedRecordMapper.insert(record);

        // 점유 해제
        occupiedRecordMapper.updateUnOccupiedAt(record.getId());

        List<BookCaseVO> usable = bookCaseService.findUsable();

        assertThat(usable).extracting(BookCaseVO::getId).contains(caseId);
    }

    @Test
    @DisplayName("전부 점유 중이면 빈 리스트 반환")
    void findUsableBookCases_allOccupied() {
        long caseId1 = bookCaseService.create(createDto("01", typeId));
        long caseId2 = bookCaseService.create(createDto("02", typeId));

        BookOwnerVO owner1 = createBookOwner("all-occ1@test.com");
        BookOwnerVO owner2 = createBookOwner("all-occ2@test.com");

        BookCaseOccupiedRecordVO r1 = new BookCaseOccupiedRecordVO();
        r1.setBookCaseId(caseId1);
        r1.setBookOwnerId(owner1.getId());
        r1.setExpirationDate(LocalDate.now().plusMonths(3));
        occupiedRecordMapper.insert(r1);

        BookCaseOccupiedRecordVO r2 = new BookCaseOccupiedRecordVO();
        r2.setBookCaseId(caseId2);
        r2.setBookOwnerId(owner2.getId());
        r2.setExpirationDate(LocalDate.now().plusMonths(3));
        occupiedRecordMapper.insert(r2);

        List<BookCaseVO> usable = bookCaseService.findUsable();

        assertThat(usable).extracting(BookCaseVO::getId)
                .doesNotContain(caseId1, caseId2);
    }

    // === occupy 테스트 ===

    @Test
    @DisplayName("점유 성공 - 빈 책장에 점유 시 레코드 생성")
    void occupy_success() {
        long caseId = bookCaseService.create(createDto("01", typeId));
        BookOwnerVO owner = createBookOwner("occupy-success@test.com");

        bookCaseService.occupy(owner.getId(), List.of(caseId), LocalDate.now().plusMonths(3));

        assertThat(bookCaseService.isOccupied(caseId)).isTrue();
        BookCaseOccupiedRecordVO record = occupiedRecordMapper.selectCurrentByBookCaseId(caseId);
        assertThat(record).isNotNull();
        assertThat(record.getBookOwnerId()).isEqualTo(owner.getId());
        assertThat(record.getBookCaseId()).isEqualTo(caseId);
    }

    @Test
    @DisplayName("점유 실패 - 이미 점유 중인 책장에 점유 시 예외")
    void occupy_alreadyOccupied_throwsException() {
        long caseId = bookCaseService.create(createDto("01", typeId));
        BookOwnerVO owner1 = createBookOwner("occupy-first@test.com");
        BookOwnerVO owner2 = createBookOwner("occupy-second@test.com");

        bookCaseService.occupy(owner1.getId(), List.of(caseId), LocalDate.now().plusMonths(3));

        assertThatThrownBy(() -> bookCaseService.occupy(owner2.getId(), List.of(caseId), LocalDate.now().plusMonths(3)))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("점유 해제 후 다른 사람이 점유 성공")
    void occupy_afterRelease_success() {
        long caseId = bookCaseService.create(createDto("01", typeId));
        BookOwnerVO owner1 = createBookOwner("occupy-release1@test.com");
        BookOwnerVO owner2 = createBookOwner("occupy-release2@test.com");

        // owner1 점유
        bookCaseService.occupy(owner1.getId(), List.of(caseId), LocalDate.now().plusMonths(3));

        // owner1 해제
        BookCaseOccupiedRecordVO record = occupiedRecordMapper.selectCurrentByBookCaseId(caseId);
        occupiedRecordMapper.updateUnOccupiedAt(record.getId());

        // owner2 점유 성공
        bookCaseService.occupy(owner2.getId(), List.of(caseId), LocalDate.now().plusMonths(3));

        assertThat(bookCaseService.isOccupied(caseId)).isTrue();
        BookCaseOccupiedRecordVO current = occupiedRecordMapper.selectCurrentByBookCaseId(caseId);
        assertThat(current.getBookOwnerId()).isEqualTo(owner2.getId());
    }

    @Test
    @DisplayName("점유 후 이용 가능 목록에서 제외됨")
    void occupy_thenNotInUsableList() {
        long caseId1 = bookCaseService.create(createDto("01", typeId));
        long caseId2 = bookCaseService.create(createDto("02", typeId));
        BookOwnerVO owner = createBookOwner("occupy-usable@test.com");

        bookCaseService.occupy(owner.getId(), List.of(caseId1), LocalDate.now().plusMonths(3));

        List<BookCaseVO> usable = bookCaseService.findUsable();
        assertThat(usable).extracting(BookCaseVO::getId)
                .contains(caseId2)
                .doesNotContain(caseId1);
    }

    @Test
    @DisplayName("점유 실패 - 존재하지 않는 책장 ID로 점유 시 예외")
    void occupy_bookCaseNotFound_throwsException() {
        BookOwnerVO owner = createBookOwner("occupy-notfound@test.com");

        assertThatThrownBy(() -> bookCaseService.occupy(owner.getId(), List.of(999999L), LocalDate.now().plusMonths(3)))
                .isInstanceOf(ApplicationException.class);
    }
}
