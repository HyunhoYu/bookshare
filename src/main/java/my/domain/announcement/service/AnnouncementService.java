package my.domain.announcement.service;

import my.domain.announcement.AnnouncementVO;
import my.domain.announcement.dto.AnnouncementCreateDto;
import my.domain.announcement.dto.AnnouncementUpdateDto;

import java.util.List;

public interface AnnouncementService {
    AnnouncementVO create(Long adminId, AnnouncementCreateDto dto);
    AnnouncementVO getById(Long id);
    List<AnnouncementVO> getAll();
    List<AnnouncementVO> getByRole(String role);
    AnnouncementVO update(Long id, AnnouncementUpdateDto dto);
    void delete(Long id);
}
