package my.domain.user.service;

import my.domain.user.UserVO;
import my.domain.user.service.auth.UserAuthService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest
@Transactional  // 테스트 후 롤백
class UserAuthServiceImplTest {

    @Autowired
    private UserAuthService userAuthService;

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("db에 insert 성공여부")
    void userCreateSuccess() {
        //given
        String code = uniqueCode();
        UserVO user = UserVO.builder()
                .name("홍길동")
                .phone("010-" + code.substring(0, 4) + "-" + code.substring(4))
                .email("test-" + code + "@test.com")
                .password("password123")
                .residentNumber(code + "-1194410")
                .build();

        //when
        int result = userAuthService.save(user);
        UserVO savedUser = userAuthService.findById(user.getId());

        //then
        Assertions.assertThat(result).isNotEqualTo(0);
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getId()).isEqualTo(user.getId());
    }



}