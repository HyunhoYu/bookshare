package my.jwt;

import io.jsonwebtoken.security.Keys;
import my.domain.user.UserVO;
import my.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String SECRET_KEY = "bookshareSecretKeyForJwtTokenGenerationMustBeLongEnough123456789";
    private final Long EXPIRATION = 86400000L; // 24시간

    @BeforeEach
    void setUp() throws Exception {
        jwtProvider = new JwtProvider();

        // 리플렉션으로 private 필드 설정
        setField(jwtProvider, "secretKey", SECRET_KEY);
        setField(jwtProvider, "exp", EXPIRATION);

        // @PostConstruct 메서드 수동 호출
        jwtProvider.init();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private UserVO createTestUser(Long id, Role role) {
        return UserVO.builder()
                .id(id)
                .role(role)
                .name("테스트유저")
                .email("test@test.com")
                .build();
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class CreateTokenTest {

        @Test
        @DisplayName("BOOK_OWNER 유저로 토큰 생성 성공")
        void createToken_BookOwner_Success() {
            // given
            UserVO user = createTestUser(1L, Role.BOOK_OWNER);

            // when
            String token = jwtProvider.createToken(user);

            // then
            assertNotNull(token);
            assertTrue(token.split("\\.").length == 3); // JWT는 3부분으로 구성
        }

        @Test
        @DisplayName("CUSTOMER 유저로 토큰 생성 성공")
        void createToken_Customer_Success() {
            // given
            UserVO user = createTestUser(2L, Role.CUSTOMER);

            // when
            String token = jwtProvider.createToken(user);

            // then
            assertNotNull(token);
        }

        @Test
        @DisplayName("ADMIN 유저로 토큰 생성 성공")
        void createToken_Admin_Success() {
            // given
            UserVO user = createTestUser(3L, Role.ADMIN);

            // when
            String token = jwtProvider.createToken(user);

            // then
            assertNotNull(token);
        }
    }

    @Nested
    @DisplayName("토큰에서 정보 추출 테스트")
    class ExtractInfoTest {

        @Test
        @DisplayName("토큰에서 userId 추출 성공")
        void getUserId_Success() {
            // given
            Long expectedUserId = 123L;
            UserVO user = createTestUser(expectedUserId, Role.BOOK_OWNER);
            String token = jwtProvider.createToken(user);

            // when
            Long actualUserId = jwtProvider.getUserId(token);

            // then
            assertEquals(expectedUserId, actualUserId);
        }

        @Test
        @DisplayName("토큰에서 role 추출 성공 - BOOK_OWNER")
        void getRole_BookOwner_Success() {
            // given
            UserVO user = createTestUser(1L, Role.BOOK_OWNER);
            String token = jwtProvider.createToken(user);

            // when
            String role = jwtProvider.getRole(token);

            // then
            assertEquals("BOOK_OWNER", role);
        }

        @Test
        @DisplayName("토큰에서 role 추출 성공 - CUSTOMER")
        void getRole_Customer_Success() {
            // given
            UserVO user = createTestUser(1L, Role.CUSTOMER);
            String token = jwtProvider.createToken(user);

            // when
            String role = jwtProvider.getRole(token);

            // then
            assertEquals("CUSTOMER", role);
        }

        @Test
        @DisplayName("토큰에서 role 추출 성공 - EMPLOYEE")
        void getRole_Employee_Success() {
            // given
            UserVO user = createTestUser(1L, Role.EMPLOYEE);
            String token = jwtProvider.createToken(user);

            // when
            String role = jwtProvider.getRole(token);

            // then
            assertEquals("EMPLOYEE", role);
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validateToken_ValidToken_ReturnsTrue() {
            // given
            UserVO user = createTestUser(1L, Role.BOOK_OWNER);
            String token = jwtProvider.createToken(user);

            // when
            boolean isValid = jwtProvider.validateToken(token);

            // then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("잘못된 토큰 검증 실패")
        void validateToken_InvalidToken_ReturnsFalse() {
            // given
            String invalidToken = "invalid.token.here";

            // when
            boolean isValid = jwtProvider.validateToken(invalidToken);

            // then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("변조된 토큰 검증 실패")
        void validateToken_TamperedToken_ReturnsFalse() {
            // given
            UserVO user = createTestUser(1L, Role.BOOK_OWNER);
            String token = jwtProvider.createToken(user);
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx"; // 서명 변조

            // when
            boolean isValid = jwtProvider.validateToken(tamperedToken);

            // then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("null 토큰 검증 실패")
        void validateToken_NullToken_ReturnsFalse() {
            // when
            boolean isValid = jwtProvider.validateToken(null);

            // then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("빈 문자열 토큰 검증 실패")
        void validateToken_EmptyToken_ReturnsFalse() {
            // when
            boolean isValid = jwtProvider.validateToken("");

            // then
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("만료 토큰 테스트")
    class ExpiredTokenTest {

        @Test
        @DisplayName("만료된 토큰 검증 실패")
        void validateToken_ExpiredToken_ReturnsFalse() throws Exception {
            // given - 만료시간을 0으로 설정
            JwtProvider expiredProvider = new JwtProvider();
            setField(expiredProvider, "secretKey", SECRET_KEY);
            setField(expiredProvider, "exp", 0L); // 즉시 만료
            expiredProvider.init();

            UserVO user = createTestUser(1L, Role.BOOK_OWNER);
            String token = expiredProvider.createToken(user);

            // 약간의 시간 지연
            Thread.sleep(10);

            // when
            boolean isValid = expiredProvider.validateToken(token);

            // then
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("다른 Secret Key로 생성된 토큰 테스트")
    class DifferentSecretKeyTest {

        @Test
        @DisplayName("다른 Secret Key로 생성된 토큰은 검증 실패")
        void validateToken_DifferentSecretKey_ReturnsFalse() throws Exception {
            // given - 다른 Secret Key로 토큰 생성
            JwtProvider otherProvider = new JwtProvider();
            setField(otherProvider, "secretKey", "differentSecretKeyForJwtTokenGenerationMustBeLongEnough999");
            setField(otherProvider, "exp", EXPIRATION);
            otherProvider.init();

            UserVO user = createTestUser(1L, Role.BOOK_OWNER);
            String tokenFromOtherProvider = otherProvider.createToken(user);

            // when - 원래 provider로 검증
            boolean isValid = jwtProvider.validateToken(tokenFromOtherProvider);

            // then
            assertFalse(isValid);
        }
    }
}
