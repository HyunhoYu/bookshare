package my.domain.bookowner.service;

import lombok.RequiredArgsConstructor;
import my.domain.book.BookMapper;
import my.domain.book.BookService;
import my.domain.book.BookVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement.service.SettlementService;
import my.domain.settlement.vo.SettlementVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookOwnerServiceImpl implements BookOwnerService{

    private final BookOwnerMapper bookOwnerMapper;
    private final BookService bookService;
    private final SettlementService settlementService;

    @Override
    public BookOwnerVO findOne(Long id) {
        return bookOwnerMapper.selectOne(id);
    }

    @Override
    public List<BookOwnerVO> findAll() {
        return bookOwnerMapper.selectAll();
    }

    @Override
    public List<BookVO> findMyBooks(Long id) {
        return bookService.findBooksByBookOwnerId(id);
    }

    @Override
    public List<BookVO> findMySoldBooks(Long bookOwnerId) {
        return bookService.findSoldBookOfBookOwner(bookOwnerId);
    }

    @Override
    public List<SettlementVO> findAllMySettlements(Long bookOwnerId) {
        return settlementService.findAll(bookOwnerId);
    }

    @Override
    public List<SettlementVO> findMySettled(Long bookOwnerId) {
        return settlementService.findSettled(bookOwnerId);
    }

    @Override
    public List<BookSoldRecordVO> findMyUnSettled(Long bookOwnerId) {
        return settlementService.findUnSettled(bookOwnerId);
    }
}
