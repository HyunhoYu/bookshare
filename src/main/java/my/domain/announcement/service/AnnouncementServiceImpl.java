package my.domain.announcement.service;

import lombok.RequiredArgsConstructor;
import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;
import my.domain.announcement.AnnouncementMapper;
import my.domain.announcement.AnnouncementVO;
import my.domain.announcement.dto.AnnouncementCreateDto;
import my.domain.announcement.dto.AnnouncementUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public AnnouncementVO create(Long adminId, AnnouncementCreateDto dto) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setAdminId(adminId);
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());
        vo.setTargetRole(dto.getTargetRole() != null ? dto.getTargetRole() : "ALL");
        vo.setIsPinned(dto.isPinned() ? 1 : 0);

        int result = announcementMapper.insert(vo);
        if (result != 1) {
            throw new ApplicationException(ErrorCode.ANNOUNCEMENT_INSERT_FAIL);
        }

        return announcementMapper.selectById(vo.getId());
    }

    @Override
    @Transactional
    public AnnouncementVO getById(Long id) {
        AnnouncementVO vo = announcementMapper.selectById(id);
        if (vo == null) throw new ApplicationException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        announcementMapper.incrementViewCount(id);
        vo.setViewCount(vo.getViewCount() + 1);
        return vo;
    }

    @Override
    public List<AnnouncementVO> getAll() {
        return announcementMapper.selectAll();
    }

    @Override
    public List<AnnouncementVO> getByRole(String role) {
        return announcementMapper.selectByTargetRole(role);
    }

    @Override
    @Transactional
    public AnnouncementVO update(Long id, AnnouncementUpdateDto dto) {
        AnnouncementVO vo = announcementMapper.selectById(id);
        if (vo == null) throw new ApplicationException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);

        if (dto.getTitle() != null) vo.setTitle(dto.getTitle());
        if (dto.getContent() != null) vo.setContent(dto.getContent());
        if (dto.getTargetRole() != null) vo.setTargetRole(dto.getTargetRole());
        if (dto.getPinned() != null) vo.setIsPinned(dto.getPinned() ? 1 : 0);

        announcementMapper.update(vo);
        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AnnouncementVO vo = announcementMapper.selectById(id);
        if (vo == null) throw new ApplicationException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        announcementMapper.softDelete(id);
    }
}
