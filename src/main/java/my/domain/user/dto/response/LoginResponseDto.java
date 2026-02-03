package my.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import my.jwt.JwtTokenResponseDto;

@Getter
@RequiredArgsConstructor
public class LoginResponseDto {

    private boolean success;
    private JwtTokenResponseDto jwtTokenResponseDto;

}
