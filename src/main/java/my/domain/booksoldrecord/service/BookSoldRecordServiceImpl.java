package my.domain.booksoldrecord.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.BookNotFoundException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.settlement_ratio.service.SettlementRatioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookSoldRecordServiceImpl implements BookSoldRecordService {

    private final BookMapper bookMapper;
    private final BookSoldRecordMapper bookSoldRecordMapper;
    private final SettlementRatioService settlementRatioService;

     /*
        1. bookId검증
        2. bookId로 book 조회
        3. set (sold_price, id)


        6. set(customer_id)

        7. set(common_code_id)
        8. book_owner_settlement_id = null
        9. settlement_ratio_id = null
         */



    @Override
    @Transactional
    public List<BookSoldRecordVO> sellBooks(List<BuyBookRequestDto> buyBookRequestDtos) {

        List<BookSoldRecordVO> result = new ArrayList<>();

        if (buyBookRequestDtos == null || buyBookRequestDtos.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_BOOK_SALE_REQUEST);
        }

        for (BuyBookRequestDto buyBookRequestDto : buyBookRequestDtos) {
            Long bookId = buyBookRequestDto.getBookId();
            BookVO bookVO = bookMapper.selectById(bookId);

            if (bookVO == null) {
                throw new BookNotFoundException(ErrorCode.BOOK_NOT_FOUND);
            }

            BookSoldRecordVO soldRecord = createSoldRecord(bookVO, buyBookRequestDto);
            result.add(soldRecord);
        }

        return result;

    }

    private BookSoldRecordVO createSoldRecord(BookVO bookVO, BuyBookRequestDto dto) {
        BookSoldRecordVO bookSoldRecordVO = new BookSoldRecordVO();
        bookSoldRecordVO.setId(bookVO.getId());
        bookSoldRecordVO.setSoldPrice(bookVO.getPrice());
        bookSoldRecordVO.setCustomerId(dto.getCustomerId());
        bookSoldRecordVO.setCommonCodeId(dto.getBuyTypeCommonCode());
        bookSoldRecordVO.setBookOwnerSettlementId(null);
        bookSoldRecordVO.setRatioId(settlementRatioService.getRatio().getId());

        int result = bookMapper.updateStateSold(bookVO.getId());

        if (result != 1) {
            throw new ApplicationException(ErrorCode.BOOK_ALREADY_SOLD);
        }

        result = bookSoldRecordMapper.insert(bookSoldRecordVO);

        if (result != 1) {
            throw new ApplicationException(ErrorCode.BOOK_SALE_RECORD_INSERT_FAIL);
        }

        return bookSoldRecordVO;
    }


}
