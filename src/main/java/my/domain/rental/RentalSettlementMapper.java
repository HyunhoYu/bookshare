package my.domain.rental;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RentalSettlementMapper {

    int insert(RentalSettlementVO vo);
    List<RentalSettlementDetailVO> selectAllDetail();
    List<RentalSettlementDetailVO> selectDetailByBookOwnerId(Long bookOwnerId);
    RentalSettlementVO selectById(Long id);
    int updateStatusPaid(Long id);
    List<RentalSettlementVO> selectUnpaidByBookOwnerId(Long bookOwnerId);
    int updateDeducted(@Param("id") Long id,
                       @Param("deductedAmount") int deductedAmount,
                       @Param("remainingAmount") int remainingAmount,
                       @Param("status") String status);
}
