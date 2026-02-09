package my.domain.booksoldrecord.service;

import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;

import java.util.List;

public interface BookSoldRecordService {

    List<BookSoldRecordVO> sellBooks(List<BuyBookRequestDto> buyBookRequestDtos);
}
