package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.Building;
import com.demo.exception.BusinessException;
import com.demo.mapper.BuildingMapper;
import com.demo.mapper.CommunityMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingMapper buildingMapper;
    private final CommunityMapper communityMapper;

    public PageResult<Map<String, Object>> page(int page, int size, Long communityId, String name, String buildingNo) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("communityId", communityId);
        p.put("name", name);
        p.put("buildingNo", buildingNo);
        long total = buildingMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? java.util.Collections.emptyList() : buildingMapper.selectPage(p);
        return new PageResult<>(page, (int) p.get("size"), total, rows);
    }

    public Building detail(Long id) {
        Building b = buildingMapper.selectById(id);
        if (b == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return b;
    }

    @Transactional
    public Long create(Building b) {
        if (communityMapper.selectById(b.getCommunityId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "小区不存在");
        }
        if (buildingMapper.countByNo(b.getCommunityId(), b.getBuildingNo(), null) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该小区下楼栋编号已存在");
        }
        b.setCreateBy(UserContext.getUsername());
        b.setUpdateBy(UserContext.getUsername());
        buildingMapper.insert(b);
        return b.getId();
    }

    @Transactional
    public void update(Long id, Building b) {
        if (buildingMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (b.getBuildingNo() != null && b.getCommunityId() != null
                && buildingMapper.countByNo(b.getCommunityId(), b.getBuildingNo(), id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该小区下楼栋编号已存在");
        }
        b.setId(id);
        b.setUpdateBy(UserContext.getUsername());
        buildingMapper.updateById(b);
    }

    @Transactional
    public void delete(Long id) {
        if (buildingMapper.countHouses(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该楼栋下存在房屋，请先删除房屋");
        }
        buildingMapper.logicDelete(id, UserContext.getUsername());
    }
}
