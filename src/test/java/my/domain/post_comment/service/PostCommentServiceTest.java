package my.domain.post_comment.service;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.bookowner_post.BookOwnerPostVO;
import my.domain.bookowner_post.dto.BookOwnerPostCreateDto;
import my.domain.bookowner_post.service.BookOwnerPostService;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.post_comment.PostCommentVO;
import my.domain.post_comment.dto.PostCommentCreateDto;
import my.domain.post_comment.dto.PostCommentUpdateDto;
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
class PostCommentServiceTest {

    @Autowired private PostCommentService commentService;
    @Autowired private BookOwnerPostService postService;
    @Autowired private BookOwnerAuthService bookOwnerAuthService;
    @Autowired private CustomerAuthService customerAuthService;

    private Long postId;
    private Long ownerId;
    private Long customerId;
    private Long customerId2;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        // BookOwner 생성
        String ownerCode = uniqueCode();
        BookOwnerVO owner = bookOwnerAuthService.signup(BookOwnerJoinRequestDto.builder()
                .name("테스트" + ownerCode).email("comment-" + ownerCode + "@test.com")
                .phone("010-" + ownerCode.substring(0, 4) + "-" + ownerCode.substring(4))
                .password("password123").residentNumber(ownerCode + "-1234567")
                .bankName("국민은행").accountNumber("123-456-789").build());
        ownerId = owner.getId();

        // 게시글 생성
        BookOwnerPostCreateDto postDto = new BookOwnerPostCreateDto();
        postDto.setTitle("댓글 테스트용 게시글");
        postDto.setContent("게시글 내용");
        BookOwnerPostVO post = postService.create(ownerId, postDto);
        postId = post.getId();

        // Customer 생성
        String custCode1 = uniqueCode();
        UserVO cust1 = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("고객1").email("cmt-" + custCode1 + "@test.com")
                .phone("010-" + custCode1.substring(0, 4) + "-" + custCode1.substring(4))
                .password("password123").residentNumber(custCode1 + "-2345678").build());
        customerId = cust1.getId();

        String custCode2 = uniqueCode();
        UserVO cust2 = customerAuthService.signup(UserJoinRequestDto.builder()
                .name("고객2").email("cmt-" + custCode2 + "@test.com")
                .phone("010-" + custCode2.substring(0, 4) + "-" + custCode2.substring(4))
                .password("password123").residentNumber(custCode2 + "-2345678").build());
        customerId2 = cust2.getId();
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateTest {

        @Test
        @DisplayName("성공 - 최상위 댓글 생성")
        void create_topLevel() {
            PostCommentCreateDto dto = new PostCommentCreateDto();
            dto.setContent("좋은 글이네요!");

            PostCommentVO result = commentService.create(postId, customerId, dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getPostId()).isEqualTo(postId);
            assertThat(result.getUserId()).isEqualTo(customerId);
            assertThat(result.getParentId()).isNull();
            assertThat(result.getContent()).isEqualTo("좋은 글이네요!");
            assertThat(result.getUserName()).isEqualTo("고객1");
            assertThat(result.getUserRole()).isEqualTo("CUSTOMER");
        }

        @Test
        @DisplayName("성공 - BookOwner도 댓글 작성 가능")
        void create_byBookOwner() {
            PostCommentCreateDto dto = new PostCommentCreateDto();
            dto.setContent("감사합니다!");

            PostCommentVO result = commentService.create(postId, ownerId, dto);

            assertThat(result.getUserId()).isEqualTo(ownerId);
            assertThat(result.getUserRole()).isEqualTo("BOOK_OWNER");
        }

        @Test
        @DisplayName("성공 - 1단계 대댓글 생성")
        void create_reply() {
            PostCommentCreateDto parentDto = new PostCommentCreateDto();
            parentDto.setContent("좋은 글이네요!");
            PostCommentVO parent = commentService.create(postId, customerId, parentDto);

            PostCommentCreateDto replyDto = new PostCommentCreateDto();
            replyDto.setContent("감사합니다!");
            replyDto.setParentId(parent.getId());

            PostCommentVO reply = commentService.create(postId, ownerId, replyDto);

            assertThat(reply.getParentId()).isEqualTo(parent.getId());
            assertThat(reply.getContent()).isEqualTo("감사합니다!");
        }

        @Test
        @DisplayName("실패 - 대댓글에 대댓글 불가 (2단계 이상)")
        void create_replyToReply_fail() {
            PostCommentCreateDto parentDto = new PostCommentCreateDto();
            parentDto.setContent("댓글");
            PostCommentVO parent = commentService.create(postId, customerId, parentDto);

            PostCommentCreateDto replyDto = new PostCommentCreateDto();
            replyDto.setContent("대댓글");
            replyDto.setParentId(parent.getId());
            PostCommentVO reply = commentService.create(postId, ownerId, replyDto);

            PostCommentCreateDto replyToReplyDto = new PostCommentCreateDto();
            replyToReplyDto.setContent("대대댓글");
            replyToReplyDto.setParentId(reply.getId());

            assertThatThrownBy(() -> commentService.create(postId, customerId, replyToReplyDto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMMENT_NOT_REPLY_TARGET);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글에 댓글")
        void create_postNotFound() {
            PostCommentCreateDto dto = new PostCommentCreateDto();
            dto.setContent("댓글");

            assertThatThrownBy(() -> commentService.create(999999L, customerId, dto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateTest {

        @Test
        @DisplayName("성공 - 본인 댓글 수정")
        void update_success() {
            PostCommentCreateDto createDto = new PostCommentCreateDto();
            createDto.setContent("원래 댓글");
            PostCommentVO created = commentService.create(postId, customerId, createDto);

            PostCommentUpdateDto updateDto = new PostCommentUpdateDto();
            updateDto.setContent("수정된 댓글");

            PostCommentVO result = commentService.update(created.getId(), customerId, updateDto);

            assertThat(result.getContent()).isEqualTo("수정된 댓글");
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 수정 시도")
        void update_forbidden() {
            PostCommentCreateDto createDto = new PostCommentCreateDto();
            createDto.setContent("댓글");
            PostCommentVO created = commentService.create(postId, customerId, createDto);

            PostCommentUpdateDto updateDto = new PostCommentUpdateDto();
            updateDto.setContent("수정");

            assertThatThrownBy(() -> commentService.update(created.getId(), customerId2, updateDto))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteTest {

        @Test
        @DisplayName("성공 - soft delete")
        void delete_success() {
            PostCommentCreateDto createDto = new PostCommentCreateDto();
            createDto.setContent("삭제할 댓글");
            PostCommentVO created = commentService.create(postId, customerId, createDto);

            commentService.delete(created.getId(), customerId);

            List<PostCommentVO> result = commentService.findByPostId(postId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 삭제 시도")
        void delete_forbidden() {
            PostCommentCreateDto createDto = new PostCommentCreateDto();
            createDto.setContent("댓글");
            PostCommentVO created = commentService.create(postId, customerId, createDto);

            assertThatThrownBy(() -> commentService.delete(created.getId(), customerId2))
                    .isInstanceOf(ApplicationException.class)
                    .extracting(e -> ((ApplicationException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class FindTest {

        @Test
        @DisplayName("게시글의 댓글 + 대댓글 계층 조회")
        void findByPostId_withReplies() {
            PostCommentCreateDto parentDto = new PostCommentCreateDto();
            parentDto.setContent("댓글1");
            PostCommentVO parent = commentService.create(postId, customerId, parentDto);

            PostCommentCreateDto replyDto = new PostCommentCreateDto();
            replyDto.setContent("대댓글1");
            replyDto.setParentId(parent.getId());
            commentService.create(postId, ownerId, replyDto);

            PostCommentCreateDto parentDto2 = new PostCommentCreateDto();
            parentDto2.setContent("댓글2");
            commentService.create(postId, customerId2, parentDto2);

            List<PostCommentVO> result = commentService.findByPostId(postId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContent()).isEqualTo("댓글1");
            assertThat(result.get(0).getReplies()).hasSize(1);
            assertThat(result.get(0).getReplies().get(0).getContent()).isEqualTo("대댓글1");
            assertThat(result.get(1).getContent()).isEqualTo("댓글2");
            assertThat(result.get(1).getReplies()).isEmpty();
        }
    }
}
