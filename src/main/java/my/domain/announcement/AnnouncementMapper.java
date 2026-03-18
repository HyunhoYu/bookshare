package my.domain.announcement;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {
    int insert(AnnouncementVO vo);
    AnnouncementVO selectById(Long id);
    List<AnnouncementVO> selectAll();
    List<AnnouncementVO> selectByTargetRole(@Param("targetRole") String targetRole);
    int update(AnnouncementVO vo);
    int softDelete(Long id);
    int incrementViewCount(Long id);
}
