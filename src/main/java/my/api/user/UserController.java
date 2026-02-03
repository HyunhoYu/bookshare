package my.api.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import my.annotation.RequireRole;
import my.common.response.ApiResponse;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import my.domain.user.service.UserService;
import my.enums.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RequireRole(Role.ADMIN)
    @GetMapping
    public ApiResponse<List<UserVO>> findAll() {
        List<UserVO> users = userService.findAll();
        return ApiResponse.success(users);
    }

    @RequireRole(Role.ADMIN)
    @GetMapping("/{id}")
    public ApiResponse<UserVO> findOne(@PathVariable Long id) {

        UserVO user = userService.findById(id);

        if (user == null) {
            return ApiResponse.badRequest("존재하지 않는 유저");
        }
        return ApiResponse.success(user);
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<UserVO> updateOne(@RequestBody UserUpdateDto userUpdateDto, @PathVariable Long id) {
        UserVO user = userService.updateOne(userUpdateDto, id);

        return ApiResponse.success("수정 완료", user);
    }

    @RequireRole(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteOne(@PathVariable Long id) {
        userService.deleteOne(id);

        return ApiResponse.success("success", null);
    }


}
