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
    public ApiResponse<UserVO> findById(@PathVariable Long id) {

        UserVO user = userService.findById(id);

        if (user == null) {
            return ApiResponse.notFound("존재하지 않는 유저");
        }
        return ApiResponse.success(user);
    }

    @RequireRole(Role.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<UserVO> update(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        UserVO user = userService.update(id, userUpdateDto);

        return ApiResponse.success("수정 완료", user);
    }

    @RequireRole(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        userService.delete(id);

        return ApiResponse.success("success", null);
    }


}
