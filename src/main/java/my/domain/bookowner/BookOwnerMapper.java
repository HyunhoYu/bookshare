package my.domain.bookowner;

import my.domain.bookowner.vo.BookOwnerVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookOwnerMapper {

    int insert(BookOwnerVO bookOwnerVO);
    BookOwnerVO selectById(Long id);
    List<BookOwnerVO> selectAll();

    BookOwnerVO selectOne(Long id);
}
