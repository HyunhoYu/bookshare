package my.domain.code.service;

import lombok.RequiredArgsConstructor;
import my.domain.code.CommonCodeMapper;
import my.domain.code.CommonCodeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonCodeServiceImpl implements CommonCodeService {

    private final CommonCodeMapper commonCodeMapper;

    @Override
    public List<CommonCodeVO> findByGroupCode(String groupCode) {
        return commonCodeMapper.selectByGroupCode(groupCode);
    }
}
