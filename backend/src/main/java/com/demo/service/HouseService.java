package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.House;
import com.demo.exception.BusinessException;
import com.demo.mapper.HouseMapper;
import com.demo.mapper.OwnerMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HouseService {

    private final HouseMapper houseMapper;
    private final OwnerMapper ownerMapper;

    public PageResult<Map<String, Object>> page(int page, int size, Long communityId, Long buildingId,
                                                String houseNo, Integer status, Integer hasOwner) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("communityId", communityId);
        p.put("buildingId", buildingId);
        p.put("houseNo", houseNo);
        p.put("status", status);
        p.put("hasOwner", hasOwner);
        long total = houseMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? java.util.Collections.emptyList() : houseMapper.selectPage(p);
        for (Map<String, Object> r : rows) {
            r.put("statusName", Dict.name(Dict.HOUSE_STATUS, intOf(r.get("status"))));
        }
        return new PageResult<>(page, (int) p.get("size"), total, rows);
    }

    public House detail(Long id) {
        House h = houseMapper.selectById(id);
        if (h == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return h;
    }

    @Transactional
    public Long create(House h) {
        if (houseMapper.countByNo(h.getBuildingId(), h.getHouseNo(), null) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该楼栋下房号已存在");
        }
        if (h.getStatus() == null) h.setStatus(1);
        h.setCreateBy(UserContext.getUsername());
        h.setUpdateBy(UserContext.getUsername());
        houseMapper.insert(h);
        return h.getId();
    }

    @Transactional
    public void update(Long id, House h) {
        if (houseMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (h.getBuildingId() != null && h.getHouseNo() != null
                && houseMapper.countByNo(h.getBuildingId(), h.getHouseNo(), id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该楼栋下房号已存在");
        }
        h.setId(id);
        h.setUpdateBy(UserContext.getUsername());
        houseMapper.updateById(h);
    }

    @Transactional
    public void delete(Long id) {
        if (houseMapper.countUnfinishedBills(id) > 0 || houseMapper.countActiveOrders(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该房屋存在未结清账单或进行中工单，不可删除");
        }
        houseMapper.logicDelete(id, UserContext.getUsername());
    }

    public List<Map<String, Object>> ownersOf(Long id) {
        if (houseMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return ownerMapper.selectOwnersOfHouse(id);
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
