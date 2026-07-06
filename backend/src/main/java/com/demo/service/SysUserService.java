package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.SysUser;
import com.demo.exception.BusinessException;
import com.demo.mapper.CommunityMapper;
import com.demo.mapper.SysUserMapper;
import com.demo.utils.PasswordUtils;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final CommunityMapper communityMapper;
    private final PasswordUtils passwordUtils;

    public PageResult<Map<String, Object>> page(Map<String, Object> params) {
        long total = sysUserMapper.countList(params);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : sysUserMapper.selectList(params);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            decorate(r);
            list.add(r);
        }
        int page = (int) params.get("page");
        int size = (int) params.get("size");
        return new PageResult<>(page, size, total, list);
    }

    private void decorate(Map<String, Object> r) {
        r.remove("password");
        r.remove("deleted");
        Object cid = r.get("communityId");
        if (cid != null) r.put("communityName", communityMapper.nameOf(((Number) cid).longValue()));
        else r.put("communityName", null);
        r.put("userTypeName", Dict.name(Dict.USER_TYPE, toInt(r.get("userType"))));
        List<Map<String, Object>> roles = sysUserMapper.selectRolesOfUser(((Number) r.get("id")).longValue());
        List<String> names = new ArrayList<>();
        for (Map<String, Object> role : roles) names.add((String) role.get("roleName"));
        r.put("roleNames", names);
    }

    public Map<String, Object> detail(Long id) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) throw new BusinessException(ResultCode.NOT_FOUND);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", u.getId());
        r.put("username", u.getUsername());
        r.put("realName", u.getRealName());
        r.put("phone", u.getPhone());
        r.put("email", u.getEmail());
        r.put("gender", u.getGender());
        r.put("avatar", u.getAvatar());
        r.put("userType", u.getUserType());
        r.put("communityId", u.getCommunityId());
        r.put("communityName", u.getCommunityId() == null ? null : communityMapper.nameOf(u.getCommunityId()));
        r.put("status", u.getStatus());
        r.put("roleIds", sysUserMapper.selectRoleIdsByUserId(id));
        List<Map<String, Object>> roles = sysUserMapper.selectRolesOfUser(id);
        List<String> names = new ArrayList<>();
        for (Map<String, Object> role : roles) names.add((String) role.get("roleName"));
        r.put("roleNames", names);
        r.put("lastLoginTime", u.getLastLoginTime());
        r.put("createTime", u.getCreateTime());
        r.put("updateTime", u.getUpdateTime());
        return r;
    }

    @Transactional
    public Long create(SysUser user, List<Long> roleIds) {
        if (sysUserMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        user.setPassword(passwordUtils.encode(user.getPassword()));
        if (user.getUserType() == null) user.setUserType(1);
        if (user.getStatus() == null) user.setStatus(1);
        user.setCreateBy(UserContext.getUsername());
        user.setUpdateBy(UserContext.getUsername());
        sysUserMapper.insert(user);
        if (roleIds != null && !roleIds.isEmpty()) {
            sysUserMapper.insertUserRoles(user.getId(), roleIds);
        }
        return user.getId();
    }

    @Transactional
    public void update(Long id, SysUser user) {
        user.setId(id);
        user.setUpdateBy(UserContext.getUsername());
        if (sysUserMapper.updateById(user) == 0) throw new BusinessException(ResultCode.NOT_FOUND);
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) throw new BusinessException(ResultCode.CONFLICT.getCode(), "超级管理员账号不可删除");
        if (sysUserMapper.logicDelete(id, UserContext.getUsername()) == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    @Transactional
    public void resetPassword(Long id, String newPwd) {
        if (newPwd == null || newPwd.length() < 6 || newPwd.length() > 20) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "密码长度需在 6-20 位之间");
        }
        if (sysUserMapper.updatePassword(id, passwordUtils.encode(newPwd), UserContext.getUsername()) == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        if (id == 1L && status == 0) throw new BusinessException(ResultCode.CONFLICT.getCode(), "不可禁用超级管理员");
        if (sysUserMapper.updateStatus(id, status, UserContext.getUsername()) == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
    }

    public Map<String, Object> rolesOf(Long id) {
        if (sysUserMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", id);
        data.put("roleIds", sysUserMapper.selectRoleIdsByUserId(id));
        data.put("roles", sysUserMapper.selectRolesOfUser(id));
        return data;
    }

    @Transactional
    public void assignRoles(Long id, List<Long> roleIds) {
        if (sysUserMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        sysUserMapper.clearUserRoles(id);
        if (roleIds != null && !roleIds.isEmpty()) {
            sysUserMapper.insertUserRoles(id, roleIds);
        }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        return ((Number) o).intValue();
    }
}
