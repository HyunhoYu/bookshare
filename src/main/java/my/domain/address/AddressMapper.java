package my.domain.address;

import my.domain.address.vo.AddressVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper {
    int insert(AddressVO addressVO);
    AddressVO selectByUserId(Long userId);
}
