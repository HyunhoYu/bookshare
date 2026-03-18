package my.domain.qna;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QnaMapper {
    int insert(QnaVO vo);
    QnaVO selectById(Long id);
    List<QnaVO> selectByCustomerId(Long customerId);
    List<QnaVO> selectAll();
    int updateAnswer(QnaVO vo);
    List<QnaVO> selectRecent(int limit);
}
