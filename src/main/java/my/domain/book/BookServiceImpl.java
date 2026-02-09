package my.domain.book;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    @Override
    public List<BookVO> findAll() {
        return bookMapper.selectAll();
    }

    @Override
    public List<BookVO> findBooksByBookOwnerId(Long id) {
        return bookMapper.selectBooksByBookOwnerId(id);
    }

    @Override
    public List<BookVO> findSoldBookOfBookOwner(Long bookOwnerId) {
        return bookMapper.selectSoldBookByBookOwnerId(bookOwnerId);
    }


}
