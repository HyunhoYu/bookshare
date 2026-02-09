package my.domain.code;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommonCodeMapper {

    CommonCodeVO selectByGroupCodeAndCodeName(@Param("groupCode") String groupCode, @Param("codeName") String codeName);
}
