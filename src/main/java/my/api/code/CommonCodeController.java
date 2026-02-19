package my.api.code;

import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.code.CommonCodeVO;
import my.domain.code.service.CommonCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common-codes")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping
    public ApiResponse<List<CommonCodeVO>> findByGroupCode(@RequestParam String groupCode) {
        return ApiResponse.success(commonCodeService.findByGroupCode(groupCode));
    }
}
