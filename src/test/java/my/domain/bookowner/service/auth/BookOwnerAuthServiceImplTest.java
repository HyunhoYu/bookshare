package my.domain.bookowner.service.auth;

import my.domain.bankaccount.BankAccountMapper;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private BookOwnerJoinRequestDto createDefaultDto() {
        String code = uniqueCode();
        return BookOwnerJoinRequestDto.builder()
                .name("홍길동")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .email("newowner-" + code + "@test.com")
                .password("password123")
                .residentNumber(code + "-1234567")
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
            BookOwnerVO result = bookOwnerAuthService.signup(dto);

            // then
            UserVO savedUser = userMapper.selectById(result.getId());
            assertThat(savedUser).isNotNull();
        }

        @Test
        @DisplayName("회원가입 시 User, BookOwner, BankAccount가 모두 같은 ID로 저장된다")
        void signup_allEntitiesHaveSameId() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();

            // when
            BookOwnerVO result = bookOwnerAuthService.signup(dto);
            Long id = result.getId();

            // then
            UserVO savedUser = userMapper.selectById(id);
            BookOwnerVO savedBookOwner = bookOwnerMapper.selectById(id);
            BankAccountVO savedBankAccount = bankAccountMapper.selectById(id);

            assertThat(savedUser).isNotNull();
            assertThat(savedBookOwner).isNotNull();
            assertThat(savedBankAccount).isNotNull();

            assertThat(savedUser.getId()).isEqualTo(id);
            assertThat(savedBookOwner.getId()).isEqualTo(id);
            assertThat(savedBankAccount.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("회원가입 시 User의 role은 BOOK_OWNER로 설정된다")
        void signup_roleIsBookOwner() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();

            // when
            BookOwnerVO result = bookOwnerAuthService.signup(dto);

            // then
            UserVO savedUser = userMapper.selectById(result.getId());
            assertThat(savedUser.getRole()).isEqualTo(Role.BOOK_OWNER);
        }

        @Test
        @DisplayName("DTO의 User 관련 필드가 올바르게 저장된다")
        void signup_userFieldsSaved() {
            // given
            String code = uniqueCode();
            BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                    .name("김철수")
                    .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                    .email("kimcs-" + code + "@test.com")
                    .password("securePass")
                    .residentNumber(code + "-1234567")
                    .bankName("국민은행")
                    .accountNumber("123-456-789")
                    .build();

            // when
            BookOwnerVO result = bookOwnerAuthService.signup(dto);

            // then
            UserVO savedUser = userMapper.selectById(result.getId());

            assertThat(savedUser.getName()).isEqualTo("김철수");
            assertThat(savedUser.getPhone()).isEqualTo(dto.getPhone());
            assertThat(savedUser.getEmail()).isEqualTo(dto.getEmail());
            assertThat(savedUser.getResidentNumber()).isEqualTo(dto.getResidentNumber());
        }

        @Test
        @DisplayName("DTO의 BankAccount 관련 필드가 올바르게 저장된다")
        void signup_bankAccountFieldsSaved() {
            // given
            String code = uniqueCode();
            BookOwnerJoinRequestDto dto = BookOwnerJoinRequestDto.builder()
                    .name("이영희")
                    .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                    .email("leeyh-" + code + "@test.com")
                    .password("password")
                    .residentNumber(code + "-2234567")
                    .bankName("우리은행")
                    .accountNumber("1002-123-456789")
                    .build();

            // when
            BookOwnerVO result = bookOwnerAuthService.signup(dto);

            // then
            BankAccountVO savedBankAccount = bankAccountMapper.selectById(result.getId());

            assertThat(savedBankAccount.getBankName()).isEqualTo("우리은행");
            assertThat(savedBankAccount.getAccountNumber()).isEqualTo("1002-123-456789");
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

            // when
            BookOwnerVO result = bookOwnerAuthService.signup(dto);
            Long id = result.getId();

            // then
            UserVO user = userMapper.selectById(id);
            BookOwnerVO bookOwner = bookOwnerMapper.selectById(id);
            BankAccountVO bankAccount = bankAccountMapper.selectById(id);

            assertThat(user.getId())
                    .isEqualTo(bookOwner.getId())
                    .isEqualTo(bankAccount.getId())
                    .isEqualTo(id);
        }

        @Test
        @DisplayName("여러 번 회원가입해도 각각 다른 ID가 부여된다")
        void multipleSignups_differentIds() {
            // given
            String code1 = uniqueCode();
            BookOwnerJoinRequestDto dto1 = BookOwnerJoinRequestDto.builder()
                    .name("사용자1")
                    .phone("010-" + code1.substring(0, 4) + "-" + code1.substring(4))
                    .email("user1-" + code1 + "@test.com")
                    .password("pass1")
                    .residentNumber(code1 + "-1111111")
                    .bankName("은행1")
                    .accountNumber("111-111-111")
                    .build();

            String code2 = uniqueCode();
            BookOwnerJoinRequestDto dto2 = BookOwnerJoinRequestDto.builder()
                    .name("사용자2")
                    .phone("010-" + code2.substring(0, 4) + "-" + code2.substring(4))
                    .email("user2-" + code2 + "@test.com")
                    .password("pass2")
                    .residentNumber(code2 + "-2222222")
                    .bankName("은행2")
                    .accountNumber("222-222-222")
                    .build();

            // when
            BookOwnerVO result1 = bookOwnerAuthService.signup(dto1);
            BookOwnerVO result2 = bookOwnerAuthService.signup(dto2);

            // then
            assertThat(result1.getId()).isNotEqualTo(result2.getId());

            UserVO user1 = userMapper.selectById(result1.getId());
            UserVO user2 = userMapper.selectById(result2.getId());
            assertThat(user1.getName()).isEqualTo("사용자1");
            assertThat(user2.getName()).isEqualTo("사용자2");
        }
    }

    @Nested
    @DisplayName("위상 정렬 검증")
    class TopologicalSortTest {

        @Test
        @DisplayName("insert 순서가 User -> BookOwner -> BankAccount 순서로 진행된다")
        void insertOrder_userFirst() {
            // given
            BookOwnerJoinRequestDto dto = createDefaultDto();

            // when
            BookOwnerVO result = bookOwnerAuthService.signup(dto);
            Long id = result.getId();

            // then
            assertThat(userMapper.selectById(id)).isNotNull();
            assertThat(bookOwnerMapper.selectById(id)).isNotNull();
            assertThat(bankAccountMapper.selectById(id)).isNotNull();
        }
    }
}
