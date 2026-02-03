package my.api.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import my.domain.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    public ApiResponse<List<UserVO>> findAll() {
        List<UserVO> users = userService.findAll();
        return ApiResponse.success(users);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserVO> findOne(@RequestParam Long id) {

        UserVO user = userService.findById(id);

        if (user == null) {
            return ApiResponse.badRequest("존재하지 않는 유저");
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserVO> updateOne(@RequestBody UserUpdateDto userUpdateDto, @RequestParam Long userId) {
        UserVO user = userService.updateOne(userUpdateDto, userId);

        return ApiResponse.success("수정 완료", user);
    }

    @DeleteMapping("{id}")
    public ApiResponse<String> deleteOne(@RequestParam Long userId) {

    }
}
