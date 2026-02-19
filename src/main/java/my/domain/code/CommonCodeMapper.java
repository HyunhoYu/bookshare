package my.domain.code;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommonCodeMapper {

    CommonCodeVO selectByGroupCodeAndCodeName(@Param("groupCode") String groupCode, @Param("codeName") String codeName);

    CommonCodeVO selectByGroupCodeAndCode(@Param("groupCode") String groupCode, @Param("code") String code);

    List<CommonCodeVO> selectByGroupCode(@Param("groupCode") String groupCode);
}
