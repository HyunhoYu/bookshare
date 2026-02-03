package my.domain.bookowner.service;

import lombok.RequiredArgsConstructor;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.vo.BookOwnerVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookOwnerServiceImpl implements BookOwnerService{

    private final BookOwnerMapper bookOwnerMapper;

    @Override
    public BookOwnerVO findOne(Long id) {
        return bookOwnerMapper.selectOne(id);
    }

    @Override
    public List<BookOwnerVO> findAll() {
        return bookOwnerMapper.selectAll();
    }


}
