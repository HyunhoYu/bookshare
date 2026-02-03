package my.domain.bookowner.service.auth;

import lombok.RequiredArgsConstructor;
import my.common.exception.DuplicateEmailException;
import my.common.exception.ErrorCode;
import my.domain.bankaccount.service.auth.BankAccountAuthService;
import my.domain.bankaccount.vo.BankAccountVO;
import my.domain.bookowner.BookOwnerMapper;
import my.domain.bookowner.dto.BookOwnerJoinRequestDto;
import my.domain.bookowner.vo.BookOwnerVO;
import my.domain.user.UserVO;
import my.domain.user.service.auth.UserAuthService;
import my.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookOwnerAuthServiceImpl implements BookOwnerAuthService {

    private final UserAuthService userAuthService;
    private final BankAccountAuthService bankAccountAuthService;
    private final BookOwnerMapper bookOwnerMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public int save(BookOwnerVO bookOwnerVO) {
        return bookOwnerMapper.insert(bookOwnerVO);
    }

    @Override
    @Transactional
    public BookOwnerVO signup(BookOwnerJoinRequestDto dto) {

        if (userAuthService.findByEmail(dto.getEmail()) != null) {
            throw new DuplicateEmailException(ErrorCode.DUPLICATE_EMAIL);
        }

        UserVO userVO = UserVO.builder()
                .role(Role.BOOK_OWNER)
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .residentNumber(dto.getResidentNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        userAuthService.save(userVO);
        Long userId = userVO.getId();

        BookOwnerVO bookOwnerVO = new BookOwnerVO();
        bookOwnerVO.setId(userId);
        bookOwnerVO.setRole(Role.BOOK_OWNER);
        bookOwnerMapper.insert(bookOwnerVO);

        BankAccountVO bankAccountVO = new BankAccountVO();
        bankAccountVO.setId(userId);
        bankAccountVO.setBankName(dto.getBankName());
        bankAccountVO.setAccountNumber(dto.getAccountNumber());
        bankAccountAuthService.save(bankAccountVO);

        return bookOwnerVO;

    }
}
