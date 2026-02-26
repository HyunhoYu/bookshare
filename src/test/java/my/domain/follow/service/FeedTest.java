package my.domain.follow.service;

import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.dto.BookOwnerPostCreateDto;
import my.domain.bookowner_post.service.BookOwnerPostService;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserJoinRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FeedTest {

    @Autowired private BookOwnerPostService postService;
    @Autowired private FollowService followService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private CustomerAuthService customerAuthService;

    private Long customerId;
    private Long ownerId1;
    private Long ownerId2;
    private Long postId1;
    private Long postId2;
    private Long postId3;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long createBookOwner() {
        String code = uniqueCode();
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("테스트" + code).email("feed-" + code + "@test.com")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .password("password123").residentNumber(code + "-1234567")
                .bankName("국민은행").accountNumber("123-456-789").build());
        return owner.getId();
    }

    private Long createPost(Long ownerId, String title) {
        BookOwnerPostCreateDto dto = new BookOwnerPostCreateDto();
        dto.setTitle(title);
        dto.setContent(title + " 내용");
        return postService.create(ownerId, dto).getId();
    }

    @BeforeEach
    void setUp() {
        ownerId1 = createBookOwner();
        ownerId2 = createBookOwner();

        // owner1의 게시글 2개
        postId1 = createPost(ownerId1, "owner1 게시글1");
        postId2 = createPost(ownerId1, "owner1 게시글2");

        // owner2의 게시글 1개
        postId3 = createPost(ownerId2, "owner2 게시글1");

        // 고객 생성
        String custCode = uniqueCode();
        UserVO customer = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("피드고객").email("feed-c-" + custCode + "@test.com")
                .phone("010-" + custCode.substring(0, 4) + "-" + custCode.substring(4))
                .password("password123").residentNumber(custCode + "-2345678").build());
        customerId = customer.getId();
    }

    @Test
    @DisplayName("팔로우한 BookOwner의 게시글만 피드에 표시")
    void feed_onlyFollowedOwners() {
        followService.follow(customerId, ownerId1);

        List<BookOwnerPostVO> feed = postService.findFeed(customerId);

        assertThat(feed).hasSize(2);
        assertThat(feed).allMatch(p -> p.getBookOwnerId().equals(ownerId1));
    }

    @Test
    @DisplayName("피드 최신순 정렬")
    void feed_newestFirst() {
        followService.follow(customerId, ownerId1);

        List<BookOwnerPostVO> feed = postService.findFeed(customerId);

        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).getCreatedAt().getTime())
                .isGreaterThanOrEqualTo(feed.get(1).getCreatedAt().getTime());
    }

    @Test
    @DisplayName("아무도 팔로우하지 않으면 빈 피드")
    void feed_emptyWhenNoFollows() {
        List<BookOwnerPostVO> feed = postService.findFeed(customerId);

        assertThat(feed).isEmpty();
    }

    @Test
    @DisplayName("여러 BookOwner 팔로우 시 모두 피드에 표시")
    void feed_multipleOwners() {
        followService.follow(customerId, ownerId1);
        followService.follow(customerId, ownerId2);

        List<BookOwnerPostVO> feed = postService.findFeed(customerId);

        assertThat(feed).hasSize(3);
    }

    @Test
    @DisplayName("삭제된 게시글은 피드에서 제외")
    void feed_excludesDeletedPosts() {
        followService.follow(customerId, ownerId1);
        postService.delete(postId1, ownerId1);

        List<BookOwnerPostVO> feed = postService.findFeed(customerId);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).getId()).isEqualTo(postId2);
    }

    @Test
    @DisplayName("언팔로우 후 피드에서 제외")
    void feed_afterUnfollow() {
        followService.follow(customerId, ownerId1);
        followService.follow(customerId, ownerId2);
        followService.unfollow(customerId, ownerId1);

        List<BookOwnerPostVO> feed = postService.findFeed(customerId);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).getBookOwnerId()).isEqualTo(ownerId2);
    }

    @Test
    @DisplayName("전체 공개 피드는 모든 게시글 표시")
    void publicFeed_showsAll() {
        List<BookOwnerPostVO> all = postService.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(3);
    }
}
