package my.domain.booksoldrecord.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.book.BookMapper;
import my.domain.book.BookVO;
import my.domain.booksoldrecord.BookSoldRecordMapper;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import my.domain.code.CommonCodeMapper;
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
    private final CommonCodeMapper commonCodeMapper;




    @Override
    @Transactional
    public List<BookSoldRecordVO> sellBooks(List<BuyBookRequestDto> buyBookRequestDtos) {

        List<BookSoldRecordVO> result = new ArrayList<>();

        if (buyBookRequestDtos == null || buyBookRequestDtos.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_BOOK_SALE_REQUEST);
        }

        for (BuyBookRequestDto buyBookRequestDto : buyBookRequestDtos) {
            Long bookId = buyBookRequestDto.getBookId();
            BookVO bookVO = requireNonNull(bookMapper.selectById(bookId), ErrorCode.BOOK_NOT_FOUND);

            BookSoldRecordVO soldRecord = createSoldRecord(bookVO, buyBookRequestDto);
            result.add(soldRecord);
        }

        return result;

    }

    private BookSoldRecordVO createSoldRecord(BookVO bookVO, BuyBookRequestDto dto) {
        requireNonNull(
                commonCodeMapper.selectByGroupCodeAndCode("BUY_TYPE", dto.getBuyTypeCommonCode()),
                ErrorCode.INVALID_BUY_TYPE);

        BookSoldRecordVO bookSoldRecordVO = new BookSoldRecordVO();
        bookSoldRecordVO.setId(bookVO.getId());
        bookSoldRecordVO.setSoldPrice(bookVO.getPrice());
        bookSoldRecordVO.setCustomerId(dto.getCustomerId());
        bookSoldRecordVO.setGroupCodeId("BUY_TYPE");
        bookSoldRecordVO.setCommonCodeId(dto.getBuyTypeCommonCode());
        bookSoldRecordVO.setBookOwnerSettlementId(null);
        bookSoldRecordVO.setRatioId(settlementRatioService.findCurrentRatio().getId());

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
