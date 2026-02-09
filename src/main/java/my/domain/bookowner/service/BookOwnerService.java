package my.domain.bookowner.service;

import my.domain.book.BookVO;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement.vo.SettlementVO;

import java.util.List;

public interface BookOwnerService {

    List<BookOwnerVO> findAll();
    BookOwnerVO findOne(Long bookOwnerId);
    List<BookVO> findMyBooks(Long bookOwnerId);
    List<BookVO> findMySoldBooks(Long bookOwnerId);
    List<SettlementVO> findAllMySettlements(Long bookOwnerId);
    List<SettlementVO> findMySettled(Long bookOwnerId);
    List<BookSoldRecordVO> findMyUnSettled(Long bookOwnerId);

}
