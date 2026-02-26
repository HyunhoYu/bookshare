package my.domain.follow.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.follow.FollowVO;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserJoinRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FollowServiceTest {

    @Autowired private FollowService followService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private CustomerAuthService customerAuthService;

    private Long customerId;
    private Long ownerId;
    private Long ownerId2;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long createBookOwner() {
        String code = uniqueCode();
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("테스트" + code).email("follow-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123").residentNumber(code + "-1234567")
                .bankName("국민은행").accountNumber("123-456-789").build());
        return owner.getId();
    }

    private Long createCustomer() {
        String code = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("고객" + code).email("flw-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123").residentNumber(code + "-2345678").build());
        return customer.getId();
    }

    @BeforeEach
    void setUp() {
        customerId = createCustomer();
        ownerId = createBookOwner();
        ownerId2 = createBookOwner();
    }

    @Nested
    @DisplayName("팔로우")
    class FollowTest {

        @Test
        @DisplayName("성공 - 팔로우")
        void follow_success() {
            FollowVO result = followService.follow(customerId, ownerId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(customerId);
            assertThat(result.getBookOwnerId()).isEqualTo(ownerId);
        }

        @Test
        @DisplayName("실패 - 중복 팔로우")
        void follow_alreadyFollowing() {
            followService.follow(customerId, ownerId);

            assertThatThrownBy(() -> followService.follow(customerId, ownerId))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ALREADY_FOLLOWING);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 고객")
        void follow_customerNotFound() {
            assertThatThrownBy(() -> followService.follow(999999L, ownerId))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 BookOwner")
        void follow_bookOwnerNotFound() {
            assertThatThrownBy(() -> followService.follow(customerId, 999999L))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.BOOK_OWNER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("언팔로우")
    class UnfollowTest {

        @Test
        @DisplayName("성공 - 언팔로우")
        void unfollow_success() {
            followService.follow(customerId, ownerId);
            followService.unfollow(customerId, ownerId);

            assertThat(followService.isFollowing(customerId, ownerId)).isFalse();
        }

        @Test
        @DisplayName("실패 - 팔로우하지 않은 상태에서 언팔로우")
        void unfollow_notFollowing() {
            assertThatThrownBy(() -> followService.unfollow(customerId, ownerId))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOLLOWING);
        }
    }

    @Nested
    @DisplayName("팔로우 조회")
    class QueryTest {

        @Test
        @DisplayName("팔로우 목록 조회")
        void getFollowList() {
            followService.follow(customerId, ownerId);
            followService.follow(customerId, ownerId2);

            List<FollowVO> result = followService.getFollowList(customerId);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("팔로우 목록 - 팔로우 없으면 빈 리스트")
        void getFollowList_empty() {
            List<FollowVO> result = followService.getFollowList(customerId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("팔로우 여부 확인 - true")
        void isFollowing_true() {
            followService.follow(customerId, ownerId);

            assertThat(followService.isFollowing(customerId, ownerId)).isTrue();
        }

        @Test
        @DisplayName("팔로우 여부 확인 - false")
        void isFollowing_false() {
            assertThat(followService.isFollowing(customerId, ownerId)).isFalse();
        }

        @Test
        @DisplayName("팔로워 수 조회")
        void getFollowerCount() {
            Long customerId2 = createCustomer();
            followService.follow(customerId, ownerId);
            followService.follow(customerId2, ownerId);

            assertThat(followService.getFollowerCount(ownerId)).isEqualTo(2);
        }

        @Test
        @DisplayName("팔로워 수 - 팔로워 없으면 0")
        void getFollowerCount_zero() {
            assertThat(followService.getFollowerCount(ownerId)).isEqualTo(0);
        }
    }
}
