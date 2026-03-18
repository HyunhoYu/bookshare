package my.domain.book_request;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookRequestMapper {
    int insert(BookRequestVO vo);
    BookRequestVO selectById(Long id);
    List<BookRequestVO> selectByCustomerId(Long customerId);
    List<BookRequestVO> selectAll();
    int updateStatus(BookRequestVO vo);
}
