package my.api.auth;

import lombok.RequiredArgsConstructor;
import my.common.response.ApiResponse;
import my.domain.bookowner.dto.request.BookOwnerJoinRequestDto;
import my.domain.bookowner.service.auth.BookOwnerAuthService;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.customer.service.auth.CustomerAuthService;
import my.domain.user.UserVO;
import my.domain.user.dto.request.LoginRequestDto;
import my.domain.user.dto.request.UserJoinRequestDto;
import my.domain.user.service.auth.UserAuthService;
import my.jwt.JwtProvider;
import my.jwt.JwtTokenResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserAuthService userAuthService;
    private final BookOwnerAuthService bookOwnerAuthService;
    private final CustomerAuthService customerAuthService;
    private final JwtProvider jwtProvider;


    @PostMapping("/login")
    public ApiResponse<JwtTokenResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        UserVO user = userAuthService.login(loginRequestDto);
        String token = jwtProvider.createToken(user);
        return ApiResponse.success(new JwtTokenResponseDto(token));
    }

    @PostMapping("/book-owner")
    public ApiResponse<JwtTokenResponseDto> registerBookOwner(@RequestBody @Valid BookOwnerJoinRequestDto dto) {
        BookOwnerVO bookOwner = bookOwnerAuthService.signup(dto);

        String token = jwtProvider.createToken(bookOwner);

        return ApiResponse.created(new JwtTokenResponseDto(token));
    }

    @PostMapping("/customer")
    public ApiResponse<JwtTokenResponseDto> registerCustomer(@RequestBody @Valid UserJoinRequestDto dto) {
        UserVO customer = customerAuthService.signup(dto);
        String token = jwtProvider.createToken(customer);
        return ApiResponse.created(new JwtTokenResponseDto(token));
    }

}
