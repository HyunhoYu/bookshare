package my.domain.bookowner;

import my.domain.bookowner.vo.BookOwnerVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookOwnerMapper {

    int insert(BookOwnerVO bookOwnerVO);

    BookOwnerVO selectById(Long id);
}
