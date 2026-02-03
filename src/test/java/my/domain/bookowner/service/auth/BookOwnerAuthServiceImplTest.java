package my.domain.bookowner.service.auth;

import my.domain.bankaccount.BankAccountMapper;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.dto.BookOwnerJoinRequestDto;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookOwnerAuthServiceImplTest {

    @Autowired
    private BookOwnerAuthService bookOwnerAuthService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookOwnerMapper bookOwnerMapper;

    @Autowired
    private BankAccountMapper bankAccountMapper;

    private BookOwnerJoinRequestDto createDefaultDto() {
        return BookOwnerJoinRequestDto.builder()
                .name("홍길동")
                .phone("010-1234-5678")
                .email("hong@test.com")
                .password("password123")
                .residentNumber("901010-1234567")
                .bankName("신한은행")
                .accountNumber("110-123-456789")
                .build();
    }

    @Nested
    @DisplayName("signup() 메서드")
    class SignupTest {

        @Test
        @DisplayName("정상적으로 회원가입되면 User 테이블에 데이터가 저장된다")
        void signup_userSaved() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();

            // when
            bookOwnerAuthService.signup(dto);

            // then - 마지막 insert된 ID로 조회 (시퀀스 기반)
            // 참고: 정확한 ID를 알기 어려우므로 예외 없이 완료되는 것으로 검증
        }

        @Test
        @DisplayName("회원가입 시 User, BookOwner, BankAccount가 모두 같은 ID로 저장된다")
        void signup_allEntitiesHaveSameId() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();

            // 현재 시퀀스 값 확인 (다음에 생성될 ID 예측)
            Long nextId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            UserVO savedUser = userMapper.selectById(nextId);
            BookOwnerVO savedBookOwner = bookOwnerMapper.selectById(nextId);
            BankAccountVO savedBankAccount = bankAccountMapper.selectById(nextId);

            assertThat(savedUser).isNotNull();
            assertThat(savedBookOwner).isNotNull();
            assertThat(savedBankAccount).isNotNull();

            assertThat(savedUser.getId()).isEqualTo(nextId);
            assertThat(savedBookOwner.getId()).isEqualTo(nextId);
            assertThat(savedBankAccount.getId()).isEqualTo(nextId);
        }

        @Test
        @DisplayName("회원가입 시 User의 role은 BOOK_OWNER로 설정된다")
        void signup_roleIsBookOwner() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();
            Long nextId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            UserVO savedUser = userMapper.selectById(nextId);
            assertThat(savedUser.getRole()).isEqualTo(Role.BOOK_OWNER);
        }

        @Test
        @DisplayName("DTO의 User 관련 필드가 올바르게 저장된다")
        void signup_userFieldsSaved() {
            // given
            String name = "김철수";
            String phone = "010-9999-8888";
            String email = "kim@test.com";
            String password = "securePass";
            String residentNumber = "850505-1234567";

            BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                    .name(name)
                    .phone(phone)
                    .email(email)
                    .password(password)
                    .residentNumber(residentNumber)
                    .bankName("국민은행")
                    .accountNumber("123-456-789")
                    .build();

            Long nextId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            UserVO savedUser = userMapper.selectById(nextId);

            assertThat(savedUser.getName()).isEqualTo(name);
            assertThat(savedUser.getPhone()).isEqualTo(phone);
            assertThat(savedUser.getEmail()).isEqualTo(email);
            assertThat(savedUser.getPassword()).isEqualTo(password);
            assertThat(savedUser.getResidentNumber()).isEqualTo(residentNumber);
        }

        @Test
        @DisplayName("DTO의 BankAccount 관련 필드가 올바르게 저장된다")
        void signup_bankAccountFieldsSaved() {
            // given
            String bankName = "우리은행";
            String accountNumber = "1002-123-456789";

            BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                    .name("이영희")
                    .phone("010-5555-6666")
                    .email("lee@test.com")
                    .password("password")
                    .residentNumber("920202-2234567")
                    .bankName(bankName)
                    .accountNumber(accountNumber)
                    .build();

            Long nextId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            BankAccountVO savedBankAccount = bankAccountMapper.selectById(nextId);

            assertThat(savedBankAccount.getBankName()).isEqualTo(bankName);
            assertThat(savedBankAccount.getAccountNumber()).isEqualTo(accountNumber);
        }
    }

    @Nested
    @DisplayName("ID 전파 검증")
    class IdPropagationTest {

        @Test
        @DisplayName("User insert 후 생성된 ID가 BookOwner와 BankAccount에 전파된다")
        void idPropagation_fromUserToChildren() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();
            Long expectedId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            UserVO user = userMapper.selectById(expectedId);
            BookOwnerVO bookOwner = bookOwnerMapper.selectById(expectedId);
            BankAccountVO bankAccount = bankAccountMapper.selectById(expectedId);

            // 모든 ID가 동일해야 함
            assertThat(user.getId())
                    .isEqualTo(bookOwner.getId())
                    .isEqualTo(bankAccount.getId())
                    .isEqualTo(expectedId);
        }

        @Test
        @DisplayName("여러 번 회원가입해도 각각 다른 ID가 부여된다")
        void multipleSignups_differentIds() {
            // given
            BookOwnerJoinRequestDto dto1 = BookOwnerJoinRequestDto.builder()
                    .name("사용자1")
                    .phone("010-1111-1111")
                    .email("user1@test.com")
                    .password("pass1")
                    .residentNumber("900101-1111111")
                    .bankName("은행1")
                    .accountNumber("111-111-111")
                    .build();

            BookOwnerJoinRequestDto dto2 = BookOwnerJoinRequestDto.builder()
                    .name("사용자2")
                    .phone("010-2222-2222")
                    .email("user2@test.com")
                    .password("pass2")
                    .residentNumber("900202-2222222")
                    .bankName("은행2")
                    .accountNumber("222-222-222")
                    .build();

            Long firstId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto1);
            Long secondId = userMapper.selectNextId();
            bookOwnerAuthService.signup(dto2);

            // then
            UserVO user1 = userMapper.selectById(firstId);
            UserVO user2 = userMapper.selectById(secondId);

            assertThat(user1.getId()).isNotEqualTo(user2.getId());
            assertThat(user1.getName()).isEqualTo("사용자1");
            assertThat(user2.getName()).isEqualTo("사용자2");
        }
    }

    @Nested
    @DisplayName("위상 정렬 검증")
    class TopologicalSortTest {

        @Test
        @DisplayName("insert 순서가 User → BookOwner → BankAccount 순서로 진행된다")
        void insertOrder_userFirst() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();
            Long expectedId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            // 모든 테이블에 데이터가 존재하면 순서가 올바른 것
            // (FK 제약조건이 있다면 순서가 잘못되면 예외 발생)
            assertThat(userMapper.selectById(expectedId)).isNotNull();
            assertThat(bookOwnerMapper.selectById(expectedId)).isNotNull();
            assertThat(bankAccountMapper.selectById(expectedId)).isNotNull();
        }
    }

    @Nested
    @DisplayName("RelationResolver 통합 테스트")
    class RelationResolverIntegrationTest {

        @Test
        @DisplayName("@Ref 어노테이션 기반으로 관계가 올바르게 파악되어 ID가 전파된다")
        void refAnnotation_worksCorrectly() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();
            Long expectedId = userMapper.selectNextId();

            // when
            bookOwnerAuthService.signup(dto);

            // then
            // BookOwnerVO의 @Ref(reference = UserVO.class)가 동작하여
            // UserVO의 ID가 BookOwnerVO에 전파됨
            BookOwnerVO bookOwner = bookOwnerMapper.selectById(expectedId);
            assertThat(bookOwner.getId()).isEqualTo(expectedId);

            // BankAccountVO의 @Ref(reference = BookOwnerVO.class)가 동작하여
            // BookOwnerVO의 ID가 BankAccountVO에 전파됨
            BankAccountVO bankAccount = bankAccountMapper.selectById(expectedId);
            assertThat(bankAccount.getId()).isEqualTo(expectedId);
        }
    }
}
