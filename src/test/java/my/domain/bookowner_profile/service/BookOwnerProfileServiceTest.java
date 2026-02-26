package my.domain.bookowner_profile.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.bookowner_profile.BookOwnerProfileVO;
import my.domain.bookowner_profile.dto.BookOwnerProfileRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookOwnerProfileServiceTest {

    @Autowired
    private BookOwnerProfileService profileService;

    @Autowired
    private BookOwnerAuthService bookOwnerAuthService;

    private Long ownerId;
    private Long ownerId2;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long createBookOwner() {
        String code = uniqueCode();
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("테스트" + code)
                .email("profile-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123")
                .residentNumber(code + "-1234567")
                .bankName("국민은행")
                .accountNumber("123-456-789")
                .build());
        return owner.getId();
    }

    private BookOwnerProfileRequestDto createProfileDto(String nickname) {
        BookOwnerProfileRequestDto dto = new BookOwnerProfileRequestDto();
        dto.setNickname(nickname);
        dto.setFavoriteBooks("이펙티브 자바, 클린 코드");
        dto.setFavoriteAuthors("조슈아 블로크, 로버트 마틴");
        dto.setFavoriteGenres("기술서적, 에세이");
        return dto;
    }

    @BeforeEach
    void setUp() {
        ownerId = createBookOwner();
        ownerId2 = createBookOwner();
    }

    @Nested
    @DisplayName("프로필 생성")
    class CreateTest {

        @Test
        @DisplayName("성공 - 프로필 생성")
        void create_success() {
            String nickname = "닉네임" + uniqueCode();
            BookOwnerProfileVO result = profileService.create(ownerId, createProfileDto(nickname));

            assertThat(result).isNotNull();
            assertThat(result.getBookOwnerId()).isEqualTo(ownerId);
            assertThat(result.getNickname()).isEqualTo(nickname);
            assertThat(result.getFavoriteBooks()).isEqualTo("이펙티브 자바, 클린 코드");
            assertThat(result.getFavoriteAuthors()).isEqualTo("조슈아 블로크, 로버트 마틴");
            assertThat(result.getFavoriteGenres()).isEqualTo("기술서적, 에세이");
            assertThat(result.getBookOwnerName()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 선택 필드 없이 생성")
        void create_withoutOptionalFields() {
            BookOwnerProfileRequestDto dto = new BookOwnerProfileRequestDto();
            dto.setNickname("닉네임" + uniqueCode());

            BookOwnerProfileVO result = profileService.create(ownerId, dto);

            assertThat(result).isNotNull();
            assertThat(result.getFavoriteBooks()).isNull();
            assertThat(result.getFavoriteAuthors()).isNull();
            assertThat(result.getFavoriteGenres()).isNull();
        }

        @Test
        @DisplayName("실패 - 이미 프로필이 존재")
        void create_alreadyExists() {
            String nickname = "닉네임" + uniqueCode();
            profileService.create(ownerId, createProfileDto(nickname));

            assertThatThrownBy(() -> profileService.create(ownerId, createProfileDto("다른" + uniqueCode())))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("실패 - 닉네임 중복")
        void create_duplicateNickname() {
            String nickname = "닉네임" + uniqueCode();
            profileService.create(ownerId, createProfileDto(nickname));

            assertThatThrownBy(() -> profileService.create(ownerId2, createProfileDto(nickname)))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 BookOwner")
        void create_bookOwnerNotFound() {
            assertThatThrownBy(() -> profileService.create(999999L, createProfileDto("닉네임" + uniqueCode())))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BOOK_OWNER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateTest {

        @Test
        @DisplayName("성공 - 프로필 수정")
        void update_success() {
            String nickname = "닉네임" + uniqueCode();
            profileService.create(ownerId, createProfileDto(nickname));

            BookOwnerProfileRequestDto updateDto = new BookOwnerProfileRequestDto();
            updateDto.setNickname("수정" + uniqueCode());
            updateDto.setFavoriteBooks("수정된 책");

            BookOwnerProfileVO result = profileService.update(ownerId, updateDto);

            assertThat(result.getFavoriteBooks()).isEqualTo("수정된 책");
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 닉네임 유지하고 다른 필드만 수정")
        void update_sameNickname() {
            String nickname = "닉네임" + uniqueCode();
            profileService.create(ownerId, createProfileDto(nickname));

            BookOwnerProfileRequestDto updateDto = createProfileDto(nickname);
            updateDto.setFavoriteBooks("변경된 책");

            BookOwnerProfileVO result = profileService.update(ownerId, updateDto);

            assertThat(result.getNickname()).isEqualTo(nickname);
            assertThat(result.getFavoriteBooks()).isEqualTo("변경된 책");
        }

        @Test
        @DisplayName("실패 - 다른 사람 닉네임으로 변경")
        void update_duplicateNickname() {
            String nickname1 = "닉네임" + uniqueCode();
            String nickname2 = "닉네임" + uniqueCode();
            profileService.create(ownerId, createProfileDto(nickname1));
            profileService.create(ownerId2, createProfileDto(nickname2));

            assertThatThrownBy(() -> profileService.update(ownerId, createProfileDto(nickname2)))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
        }

        @Test
        @DisplayName("실패 - 프로필이 없는 상태에서 수정")
        void update_profileNotFound() {
            assertThatThrownBy(() -> profileService.update(ownerId, createProfileDto("닉네임" + uniqueCode())))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("프로필 조회")
    class FindTest {

        @Test
        @DisplayName("성공 - BookOwner ID로 조회")
        void findByBookOwnerId_success() {
            String nickname = "닉네임" + uniqueCode();
            profileService.create(ownerId, createProfileDto(nickname));

            BookOwnerProfileVO result = profileService.findByBookOwnerId(ownerId);

            assertThat(result).isNotNull();
            assertThat(result.getNickname()).isEqualTo(nickname);
        }

        @Test
        @DisplayName("프로필 없으면 null 반환")
        void findByBookOwnerId_notFound() {
            BookOwnerProfileVO result = profileService.findByBookOwnerId(ownerId);

            assertThat(result).isNull();
        }
    }
}
