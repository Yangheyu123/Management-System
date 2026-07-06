package com.demo.service;

import com.demo.common.ResultCode;
import com.demo.entity.SysRole;
import com.demo.exception.BusinessException;
import com.demo.mapper.SysRoleMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    public Object list(Map<String, Object> params, boolean pageMode) {
        List<Map<String, Object>> rows = sysRoleMapper.selectList(params);
        if (!pageMode) {
            return rows;
        }
        long total = rows.size(); // 角色量小，前端分页直接切片或返回全部由前端控制
        return rows;
    }

    public Map<String, Object> detail(Long id) {
        SysRole r = sysRoleMapper.selectById(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", r.getId());
        data.put("roleName", r.getRoleName());
        data.put("roleCode", r.getRoleCode());
        data.put("description", r.getDescription());
        data.put("status", r.getStatus());
        data.put("permissionIds", sysRoleMapper.selectPermissionIds(id));
        data.put("createTime", r.getCreateTime());
        data.put("updateTime", r.getUpdateTime());
        return data;
    }

    @Transactional
    public Long create(SysRole role) {
        if (existCode(role.getRoleCode(), null)) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "角色编码已存在");
        }
        if (role.getStatus() == null) role.setStatus(1);
        role.setCreateBy(UserContext.getUsername());
        role.setUpdateBy(UserContext.getUsername());
        sysRoleMapper.insert(role);
        return role.getId();
    }

    @Transactional
    public void update(Long id, SysRole role) {
        SysRole exists = sysRoleMapper.selectById(id);
        if (exists == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (isBuiltin(exists.getRoleCode()) && role.getRoleName() != null && !role.getRoleName().equals(exists.getRoleName())) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "内置角色不可改名");
        }
        role.setId(id);
        role.setUpdateBy(UserContext.getUsername());
        sysRoleMapper.updateById(role);
    }

    @Transactional
    public void delete(Long id) {
        SysRole r = sysRoleMapper.selectById(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (isBuiltin(r.getRoleCode())) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "内置角色不可删除");
        }
        if (sysRoleMapper.countUsers(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该角色下仍有用户，请先解除关联");
        }
        sysRoleMapper.logicDelete(id, UserContext.getUsername());
    }

    public Map<String, Object> permissionsOf(Long id) {
        if (sysRoleMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        Map<String, Object> data = new HashMap<>();
        data.put("roleId", id);
        data.put("permissionIds", sysRoleMapper.selectPermissionIds(id));
        return data;
    }

    @Transactional
    public void assignPermissions(Long id, List<Long> permIds) {
        if (sysRoleMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        sysRoleMapper.clearPermissions(id);
        if (permIds != null && !permIds.isEmpty()) {
            sysRoleMapper.insertPermissions(id, permIds);
        }
    }

    private boolean existCode(String code, Long id) {
        for (String c : sysRoleMapper.existsCodes()) {
            if (c.equals(code)) return true;
        }
        return false;
    }

    private boolean isBuiltin(String code) {
        return code != null && (code.equals("SUPER_ADMIN") || code.equals("PROPERTY_MANAGER")
                || code.equals("TOLL_COLLECTOR") || code.equals("REPAIRMAN") || code.equals("OWNER"));
    }
}
