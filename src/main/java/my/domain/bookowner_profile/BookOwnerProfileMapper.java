package my.domain.bookowner_profile;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookOwnerProfileMapper {
    int insert(BookOwnerProfileVO vo);
    int update(BookOwnerProfileVO vo);
    BookOwnerProfileVO selectByBookOwnerId(Long bookOwnerId);
    BookOwnerProfileVO selectByNickname(String nickname);
    List<BookOwnerProfileVO> selectAll();
}
