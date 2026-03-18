package my.domain.qna.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.customer.CustomerMapper;
import my.domain.notification.service.NotificationService;
import my.domain.qna.QnaMapper;
import my.domain.qna.QnaVO;
import my.domain.qna.dto.QnaAnswerDto;
import my.domain.qna.dto.QnaCreateDto;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaServiceImpl implements QnaService {

    private final QnaMapper qnaMapper;
    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public QnaVO create(Long customerId, QnaCreateDto dto) {
        requireNonNull(customerMapper.selectById(customerId), ErrorCode.CUSTOMER_NOT_FOUND);

        QnaVO vo = new QnaVO();
        vo.setCustomerId(customerId);
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());

        int result = qnaMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.QNA_INSERT_FAIL);
        }

        notifyAdmins(vo);

        return qnaMapper.selectById(vo.getId());
    }

    @Override
    @Transactional
    public QnaVO answer(Long qnaId, Long adminId, QnaAnswerDto dto) {
        QnaVO existing = requireNonNull(qnaMapper.selectById(qnaId), ErrorCode.QNA_NOT_FOUND);

        if (existing.getAnswer() != null) {
            throw new ApplicationException(ErrorCode.QNA_ALREADY_ANSWERED);
        }

        existing.setAnswer(dto.getAnswer());
        existing.setAnsweredBy(adminId);
        qnaMapper.updateAnswer(existing);

        notificationService.create(
                existing.getCustomerId(),
                "QNA_ANSWER",
                "QnA [" + existing.getTitle() + "]에 답변이 등록되었습니다.",
                qnaId
        );

        return qnaMapper.selectById(qnaId);
    }

    @Override
    public QnaVO findById(Long id) {
        return qnaMapper.selectById(id);
    }

    @Override
    public List<QnaVO> findByCustomerId(Long customerId) {
        return qnaMapper.selectByCustomerId(customerId);
    }

    @Override
    public List<QnaVO> findAll() {
        return qnaMapper.selectAll();
    }

    private void notifyAdmins(QnaVO qna) {
        List<UserVO> allUsers = userMapper.selectAll();
        for (UserVO user : allUsers) {
            if (Role.ADMIN.equals(user.getRole())) {
                notificationService.create(
                        user.getId(),
                        "QNA",
                        "새 QnA 질문: [" + qna.getTitle() + "]",
                        qna.getId()
                );
            }
        }
    }
}
