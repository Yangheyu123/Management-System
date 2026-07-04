package com.demo.service;

import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.Community;
import com.demo.exception.BusinessException;
import com.demo.mapper.CommunityMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;

    public PageResult<Map<String, Object>> page(int page, int size, String name, String address, Integer status) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("name", name);
        p.put("address", address);
        p.put("status", status);
        long total = communityMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? java.util.Collections.emptyList() : communityMapper.selectPage(p);
        return new PageResult<>(page, (int) p.get("size"), total, rows);
    }

    public List<Map<String, Object>> all() {
        return communityMapper.selectAll();
    }

    public Community detail(Long id) {
        Community c = communityMapper.selectById(id);
        if (c == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return c;
    }

    @Transactional
    public Long create(Community c) {
        if (communityMapper.countByName(c.getName(), null) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "小区名已存在");
        }
        if (c.getStatus() == null) c.setStatus(1);
        c.setCreateBy(UserContext.getUsername());
        c.setUpdateBy(UserContext.getUsername());
        communityMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void update(Long id, Community c) {
        Community exists = communityMapper.selectById(id);
        if (exists == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (c.getName() != null && communityMapper.countByName(c.getName(), id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "小区名已存在");
        }
        c.setId(id);
        c.setUpdateBy(UserContext.getUsername());
        communityMapper.updateById(c);
    }

    @Transactional
    public void delete(Long id) {
        if (communityMapper.countBuildings(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该小区下存在楼栋，请先删除楼栋");
        }
        communityMapper.logicDelete(id, UserContext.getUsername());
    }
}
