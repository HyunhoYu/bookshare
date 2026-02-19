package my.domain.code.service;

import my.domain.code.CommonCodeVO;

import java.util.List;

public interface CommonCodeService {

    List<CommonCodeVO> findByGroupCode(String groupCode);
}
