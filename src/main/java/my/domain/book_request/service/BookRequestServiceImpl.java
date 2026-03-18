package my.domain.book_request.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book_request.BookRequestMapper;
import my.domain.book_request.BookRequestVO;
import my.domain.book_request.dto.BookRequestCreateDto;
import my.domain.book_request.dto.BookRequestStatusUpdateDto;
import my.domain.customer.CustomerMapper;
import my.domain.notification.service.NotificationService;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookRequestServiceImpl implements BookRequestService {

    private final BookRequestMapper bookRequestMapper;
    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public BookRequestVO create(Long customerId, BookRequestCreateDto dto) {
        requireNonNull(customerMapper.selectById(customerId), ErrorCode.CUSTOMER_NOT_FOUND);

        BookRequestVO vo = new BookRequestVO();
        vo.setCustomerId(customerId);
        vo.setIsbn(dto.getIsbn());
        vo.setBookTitle(dto.getBookTitle());
        vo.setAuthor(dto.getAuthor());
        vo.setPublisher(dto.getPublisher());
        vo.setThumbnailUrl(dto.getThumbnailUrl());

        int result = bookRequestMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.BOOK_REQUEST_INSERT_FAIL);
        }

        notifyAdmins(vo);

        return bookRequestMapper.selectById(vo.getId());
    }

    @Override
    public List<BookRequestVO> findByCustomerId(Long customerId) {
        return bookRequestMapper.selectByCustomerId(customerId);
    }

    @Override
    public List<BookRequestVO> findAll() {
        return bookRequestMapper.selectAll();
    }

    @Override
    @Transactional
    public BookRequestVO updateStatus(Long requestId, BookRequestStatusUpdateDto dto) {
        BookRequestVO existing = requireNonNull(
                bookRequestMapper.selectById(requestId),
                ErrorCode.BOOK_REQUEST_NOT_FOUND
        );

        if (!"PENDING".equals(existing.getStatus())) {
            throw new ApplicationException(ErrorCode.BOOK_REQUEST_ALREADY_PROCESSED);
        }

        existing.setStatus(dto.getStatus());
        existing.setAdminComment(dto.getAdminComment());

        bookRequestMapper.updateStatus(existing);

        String statusLabel = "APPROVED".equals(dto.getStatus()) ? "승인" : "거절";
        notificationService.create(
                existing.getCustomerId(),
                "BOOK_REQUEST_STATUS_CHANGE",
                "입고 요청 [" + existing.getBookTitle() + "]이(가) " + statusLabel + "되었습니다.",
                requestId
        );

        return bookRequestMapper.selectById(requestId);
    }

    @Override
    public BookRequestVO findById(Long id) {
        return bookRequestMapper.selectById(id);
    }

    private void notifyAdmins(BookRequestVO bookRequest) {
        List<UserVO> allUsers = userMapper.selectAll();
        for (UserVO user : allUsers) {
            if (my.enums.Role.ADMIN.equals(user.getRole())) {
                notificationService.create(
                        user.getId(),
                        "BOOK_REQUEST",
                        "새 입고 요청: [" + bookRequest.getBookTitle() + "] - " + bookRequest.getIsbn(),
                        bookRequest.getId()
                );
            }
        }
    }
}
