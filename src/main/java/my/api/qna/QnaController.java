package my.api.qna;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.qna.QnaVO;
import my.domain.qna.dto.QnaAnswerDto;
import my.domain.qna.dto.QnaCreateDto;
import my.domain.qna.service.QnaService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
public class QnaController {

    private final QnaService qnaService;

    @RequireRole(Role.CUSTOMER)
    @PostMapping
    public ApiResponse<QnaVO> create(
            HttpServletRequest request,
            @RequestBody @Valid QnaCreateDto dto) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.created(qnaService.create(customerId, dto));
    }

    @RequireRole(Role.CUSTOMER)
    @GetMapping("/my")
    public ApiResponse<List<QnaVO>> findMyQuestions(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("userId");
        return ApiResponse.success(qnaService.findByCustomerId(customerId));
    }

    @RequireRole({Role.ADMIN, Role.CUSTOMER})
    @GetMapping("/{id}")
    public ApiResponse<QnaVO> findById(@PathVariable("id") Long id) {
        QnaVO qna = qnaService.findById(id);
        if (qna == null) {
            return ApiResponse.notFound("존재하지 않는 QnA입니다");
        }
        return ApiResponse.success(qna);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<QnaVO>> findAll() {
        return ApiResponse.success(qnaService.findAll());
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}/answer")
    public ApiResponse<QnaVO> answer(
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @RequestBody @Valid QnaAnswerDto dto) {
        Long adminId = (Long) request.getAttribute("userId");
        return ApiResponse.success(qnaService.answer(id, adminId, dto));
    }
}
