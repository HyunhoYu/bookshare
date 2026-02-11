package my.domain.customer.service;

import static my.common.util.EntityUtil.requireNonNull;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.customer.CustomerMapper;
import my.domain.user.UserVO;
import my.domain.user.dto.request.UserUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;

    @Override
    public List<UserVO> findAll() {
        return customerMapper.selectAll();
    }

    @Override
    public UserVO findById(Long id) {
        return requireNonNull(customerMapper.selectById(id), ErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Override
    @Transactional
    public UserVO update(UserUpdateDto dto) {
        requireNonNull(customerMapper.selectById(dto.getId()), ErrorCode.CUSTOMER_NOT_FOUND);
        customerMapper.update(dto);
        return customerMapper.selectById(dto.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireNonNull(customerMapper.selectById(id), ErrorCode.CUSTOMER_NOT_FOUND);
        customerMapper.softDeleteOne(id);
    }
}
