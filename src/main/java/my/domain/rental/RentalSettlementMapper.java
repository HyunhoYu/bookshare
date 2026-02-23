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
    List<RentalSettlementVO> selectOverdueUnpaidByBookOwnerId(@Param("bookOwnerId") Long bookOwnerId,
                                                              @Param("currentMonth") String currentMonth);
    int updateDeducted(@Param("id") Long id,
                       @Param("deductedAmount") int deductedAmount,
                       @Param("remainingAmount") int remainingAmount,
                       @Param("status") String status);
    int update(RentalSettlementVO vo);
    int updateTargetMonth(@Param("id") Long id, @Param("targetMonth") String targetMonth);
    List<RentalSettlementVO> selectOverdueByOccupiedRecordId(@Param("occupiedRecordId") Long occupiedRecordId,
                                                              @Param("currentMonth") String currentMonth);
}
