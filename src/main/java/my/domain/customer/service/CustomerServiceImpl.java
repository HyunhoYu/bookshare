package my.domain.customer.service;

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
    public UserVO findOne(Long id) {
        UserVO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new ApplicationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        return customer;
    }

    @Override
    @Transactional
    public UserVO update(UserUpdateDto dto) {
        UserVO customer = customerMapper.selectById(dto.getId());
        if (customer == null) {
            throw new ApplicationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        customerMapper.updateOne(dto);
        return customerMapper.selectById(dto.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserVO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new ApplicationException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        customerMapper.softDeleteOne(id);
    }
}
