package my.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import my.domain.user.UserVO;
import my.enums.Role;
import my.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    private JwtFilter jwtFilter;
    private JwtProvider jwtProvider;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    private final String SECRET_KEY = "bookshareSecretKeyForJwtTokenGenerationMustBeLongEnough123456789";
    private final Long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() throws Exception {
        // JwtProvider 설정
        jwtProvider = new JwtProvider();
        setField(jwtProvider, "secretKey", SECRET_KEY);
        setField(jwtProvider, "exp", EXPIRATION);
        jwtProvider.init();

        // JwtFilter 설정
        jwtFilter = new JwtFilter(jwtProvider);

        // Mock 객체 설정
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private String createValidToken(Long userId, Role role) {
        UserVO user = UserVO.builder()
                .id(userId)
                .role(role)
                .build();
        return jwtProvider.createToken(user);
    }

    @Nested
    @DisplayName("유효한 토큰으로 요청 시")
    class ValidTokenTest {

        @Test
        @DisplayName("request에 userId가 설정된다")
        void doFilter_ValidToken_SetsUserId() throws ServletException, IOException {
            // given
            Long expectedUserId = 123L;
            String token = createValidToken(expectedUserId, Role.BOOK_OWNER);
            request.addHeader("Authorization", "Bearer " + token);

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertEquals(expectedUserId, request.getAttribute("userId"));
        }

        @Test
        @DisplayName("request에 userRole이 설정된다")
        void doFilter_ValidToken_SetsUserRole() throws ServletException, IOException {
            // given
            String token = createValidToken(1L, Role.BOOK_OWNER);
            request.addHeader("Authorization", "Bearer " + token);

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertEquals("BOOK_OWNER", request.getAttribute("userRole"));
        }

        @Test
        @DisplayName("filterChain.doFilter가 호출된다")
        void doFilter_ValidToken_CallsFilterChain() throws ServletException, IOException {
            // given
            String token = createValidToken(1L, Role.BOOK_OWNER);
            request.addHeader("Authorization", "Bearer " + token);

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("CUSTOMER role로 요청 시 userRole에 CUSTOMER가 설정된다")
        void doFilter_CustomerRole_SetsCustomerRole() throws ServletException, IOException {
            // given
            String token = createValidToken(1L, Role.CUSTOMER);
            request.addHeader("Authorization", "Bearer " + token);

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertEquals("CUSTOMER", request.getAttribute("userRole"));
        }
    }

    @Nested
    @DisplayName("토큰이 없는 요청 시")
    class NoTokenTest {

        @Test
        @DisplayName("Authorization 헤더 없이 요청 시 userId가 설정되지 않는다")
        void doFilter_NoAuthHeader_NoUserId() throws ServletException, IOException {
            // given - Authorization 헤더 없음

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertNull(request.getAttribute("userId"));
            assertNull(request.getAttribute("userRole"));
        }

        @Test
        @DisplayName("Authorization 헤더 없어도 filterChain은 호출된다")
        void doFilter_NoAuthHeader_CallsFilterChain() throws ServletException, IOException {
            // given - Authorization 헤더 없음

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("잘못된 토큰으로 요청 시")
    class InvalidTokenTest {

        @Test
        @DisplayName("잘못된 토큰으로 요청 시 userId가 설정되지 않는다")
        void doFilter_InvalidToken_NoUserId() throws ServletException, IOException {
            // given
            request.addHeader("Authorization", "Bearer invalid.token.here");

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertNull(request.getAttribute("userId"));
            assertNull(request.getAttribute("userRole"));
        }

        @Test
        @DisplayName("잘못된 토큰이어도 filterChain은 호출된다")
        void doFilter_InvalidToken_CallsFilterChain() throws ServletException, IOException {
            // given
            request.addHeader("Authorization", "Bearer invalid.token.here");

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Bearer 접두사 없는 토큰은 무시된다")
        void doFilter_NoBearerPrefix_NoUserId() throws ServletException, IOException {
            // given
            String token = createValidToken(1L, Role.BOOK_OWNER);
            request.addHeader("Authorization", token); // Bearer 없음

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertNull(request.getAttribute("userId"));
        }

        @Test
        @DisplayName("Bearer만 있고 토큰이 없는 경우")
        void doFilter_OnlyBearer_NoUserId() throws ServletException, IOException {
            // given
            request.addHeader("Authorization", "Bearer ");

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertNull(request.getAttribute("userId"));
        }
    }

    @Nested
    @DisplayName("변조된 토큰으로 요청 시")
    class TamperedTokenTest {

        @Test
        @DisplayName("변조된 토큰으로 요청 시 userId가 설정되지 않는다")
        void doFilter_TamperedToken_NoUserId() throws ServletException, IOException {
            // given
            String token = createValidToken(1L, Role.BOOK_OWNER);
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";
            request.addHeader("Authorization", "Bearer " + tamperedToken);

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertNull(request.getAttribute("userId"));
            assertNull(request.getAttribute("userRole"));
        }
    }

    @Nested
    @DisplayName("만료된 토큰으로 요청 시")
    class ExpiredTokenTest {

        @Test
        @DisplayName("만료된 토큰으로 요청 시 userId가 설정되지 않는다")
        void doFilter_ExpiredToken_NoUserId() throws Exception {
            // given - 만료시간 0인 Provider로 토큰 생성
            JwtProvider expiredProvider = new JwtProvider();
            setField(expiredProvider, "secretKey", SECRET_KEY);
            setField(expiredProvider, "exp", 0L);
            expiredProvider.init();

            UserVO user = UserVO.builder()
                    .id(1L)
                    .role(Role.BOOK_OWNER)
                    .build();
            String expiredToken = expiredProvider.createToken(user);

            Thread.sleep(10); // 토큰 만료 대기

            request.addHeader("Authorization", "Bearer " + expiredToken);

            // when
            jwtFilter.doFilter(request, response, filterChain);

            // then
            assertNull(request.getAttribute("userId"));
        }
    }
}
