package my.domain.qna.service;

import my.domain.qna.QnaVO;
import my.domain.qna.dto.QnaAnswerDto;
import my.domain.qna.dto.QnaCreateDto;

import java.util.List;

public interface QnaService {
    QnaVO create(Long customerId, QnaCreateDto dto);
    QnaVO answer(Long qnaId, Long adminId, QnaAnswerDto dto);
    QnaVO findById(Long id);
    List<QnaVO> findByCustomerId(Long customerId);
    List<QnaVO> findAll();
}
