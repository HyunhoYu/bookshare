package my.api.book_sold_record;

import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.booksoldrecord.dto.BuyBookRequestDto;
import my.domain.booksoldrecord.service.BookSoldRecordService;
import my.domain.booksoldrecord.vo.BookSoldRecordVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-sale-records")
public class BookSoldRecordController {

    private final BookSoldRecordService bookSoldRecordService;


    @PostMapping()
    public ApiResponse<List<BookSoldRecordVO>> sellBooks(@RequestBody List<BuyBookRequestDto> buyBookRequestDtoList) {

        List<BookSoldRecordVO> bookSoldRecordVOS = bookSoldRecordService.sellBooks(buyBookRequestDtoList);

        return ApiResponse.success(bookSoldRecordVOS);
    }
}
