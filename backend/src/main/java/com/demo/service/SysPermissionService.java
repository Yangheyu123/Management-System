package com.demo.service;

import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.SysPermission;
import com.demo.exception.BusinessException;
import com.demo.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    public List<Map<String, Object>> tree() {
        List<SysPermission> all = sysPermissionMapper.selectAll();
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (SysPermission p : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", p.getId());
            node.put("parentId", p.getParentId());
            node.put("permName", p.getPermName());
            node.put("permCode", p.getPermCode());
            node.put("type", p.getType());
            node.put("path", p.getPath());
            node.put("icon", p.getIcon());
            node.put("sort", p.getSort());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(p.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (SysPermission p : all) {
            Map<String, Object> node = nodeMap.get(p.getId());
            if (p.getParentId() == null || p.getParentId() == 0 || !nodeMap.containsKey(p.getParentId())) {
                roots.add(node);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) nodeMap.get(p.getParentId()).get("children");
                children.add(node);
            }
        }
        return roots;
    }

    public PageResult<Map<String, Object>> page(Map<String, Object> params) {
        long total = sysPermissionMapper.countPage(params);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : sysPermissionMapper.selectPage(params);
        return new PageResult<>((int) params.get("page"), (int) params.get("size"), total, rows);
    }

    @Transactional
    public Long create(SysPermission perm) {
        if (existCode(perm.getPermCode())) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "权限编码已存在");
        }
        if (perm.getSort() == null) perm.setSort(0);
        sysPermissionMapper.insert(perm);
        return perm.getId();
    }

    @Transactional
    public void update(Long id, SysPermission perm) {
        SysPermission exists = sysPermissionMapper.selectById(id);
        if (exists == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (perm.getParentId() != null && perm.getParentId().equals(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能将父节点设为自己");
        }
        perm.setId(id);
        sysPermissionMapper.updateById(perm);
    }

    @Transactional
    public void delete(Long id) {
        if (sysPermissionMapper.countChildren(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "存在子节点，请先删除子节点");
        }
        sysPermissionMapper.clearRoleRef(id);
        sysPermissionMapper.logicDelete(id);
    }

    private boolean existCode(String code) {
        for (String c : sysPermissionMapper.selectAllCodes()) {
            if (c.equals(code)) return true;
        }
        return false;
    }
}
