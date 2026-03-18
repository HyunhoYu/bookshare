package my.domain.user.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.address.AddressMapper;
import my.domain.address.vo.AddressVO;
import my.domain.user.UserMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.AccountUpdateDto;
import my.domain.user.dto.request.PasswordChangeDto;
import my.domain.user.dto.request.UserUpdateDto;
import my.domain.user.dto.response.MyAccountDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyAccountServiceImpl implements MyAccountService {

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MyAccountDto getMyAccount(Long userId) {
        UserVO user = userMapper.selectById(userId);
        if (user == null) throw new ApplicationException(ErrorCode.USER_NOT_FOUND);

        AddressVO address = addressMapper.selectByUserId(userId);

        MyAccountDto.MyAccountDtoBuilder builder = MyAccountDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole());

        if (address != null) {
            builder.city(address.getCity())
                   .loadAddr(address.getLoadAddr())
                   .specificAddr(address.getSpecificAddr());
        }

        return builder.build();
    }

    @Override
    @Transactional
    public MyAccountDto updateMyAccount(Long userId, AccountUpdateDto dto) {
        UserVO user = userMapper.selectById(userId);
        if (user == null) throw new ApplicationException(ErrorCode.USER_NOT_FOUND);

        // Update user info (name, phone)
        if (dto.getName() != null || dto.getPhone() != null) {
            // Check phone duplicate
            if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())) {
                UserVO existing = userMapper.selectByPhone(dto.getPhone());
                if (existing != null) throw new ApplicationException(ErrorCode.DUPLICATE_PHONE);
            }

            UserUpdateDto userUpdateDto = new UserUpdateDto();
            userUpdateDto.setId(userId);
            userUpdateDto.setName(dto.getName());
            userUpdateDto.setPhone(dto.getPhone());
            userMapper.update(userUpdateDto);
        }

        // Update address
        if (dto.getCity() != null || dto.getLoadAddr() != null || dto.getSpecificAddr() != null) {
            AddressVO address = addressMapper.selectByUserId(userId);
            if (address != null) {
                AddressVO updated = new AddressVO();
                updated.setUserId(userId);
                updated.setCity(dto.getCity() != null ? dto.getCity() : address.getCity());
                updated.setLoadAddr(dto.getLoadAddr() != null ? dto.getLoadAddr() : address.getLoadAddr());
                updated.setSpecificAddr(dto.getSpecificAddr() != null ? dto.getSpecificAddr() : address.getSpecificAddr());
                addressMapper.update(updated);
            } else {
                AddressVO newAddr = new AddressVO();
                newAddr.setUserId(userId);
                newAddr.setCity(dto.getCity());
                newAddr.setLoadAddr(dto.getLoadAddr());
                newAddr.setSpecificAddr(dto.getSpecificAddr());
                addressMapper.insert(newAddr);
            }
        }

        return getMyAccount(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeDto dto) {
        UserVO user = userMapper.selectById(userId);
        if (user == null) throw new ApplicationException(ErrorCode.USER_NOT_FOUND);

        // Verify current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new ApplicationException(ErrorCode.PASSWORD_MISMATCH);
        }

        String encoded = passwordEncoder.encode(dto.getNewPassword());
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setId(userId);
        updateDto.setPassword(encoded);
        userMapper.update(updateDto);
    }
}
