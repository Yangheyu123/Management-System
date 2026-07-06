package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.ResultCode;
import com.demo.dto.LoginDTO;
import com.demo.entity.SysUser;
import com.demo.exception.BusinessException;
import com.demo.mapper.CommunityMapper;
import com.demo.mapper.SysUserMapper;
import com.demo.security.CurrentUserHolder;
import com.demo.utils.JwtUtils;
import com.demo.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;
    private final CurrentUserHolder holder;
    private final CommunityMapper communityMapper;

    @Transactional
    public Map<String, Object> login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
        if (roles == null) roles = new ArrayList<>();

        List<String> perms;
        if (roles.contains("SUPER_ADMIN")) {
            perms = Collections.singletonList("*");
        } else {
            perms = sysUserMapper.selectPermissionsByUserId(user.getId());
        }

        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRealName(), roles);
        sysUserMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("userType", user.getUserType());
        userInfo.put("communityId", user.getCommunityId());
        userInfo.put("roles", roles);
        userInfo.put("permissions", perms);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expiresAt", LocalDateTime.now().plusSeconds(jwtUtils.getExpireSeconds())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("userInfo", userInfo);
        return data;
    }

    public Map<String, Object> currentInfo() {
        Long userId = com.demo.utils.UserContext.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.UNAUTHORIZED);

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        List<Map<String, Object>> roleRows = sysUserMapper.selectRolesOfUser(userId);
        List<String> roleNames = new ArrayList<>();
        for (Map<String, Object> r : roleRows) roleNames.add((String) r.get("roleName"));

        List<String> perms;
        if (roles.contains("SUPER_ADMIN")) {
            perms = Collections.singletonList("*");
        } else {
            perms = sysUserMapper.selectPermissionsByUserId(userId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("gender", user.getGender());
        data.put("genderName", Dict.name(Dict.GENDER, user.getGender()));
        data.put("userType", user.getUserType());
        data.put("userTypeName", Dict.name(Dict.USER_TYPE, user.getUserType()));
        data.put("communityId", user.getCommunityId());
        data.put("communityName", communityMapper.nameOf(user.getCommunityId()));
        data.put("roles", roles);
        data.put("roleNames", roleNames);
        data.put("permissions", perms);
        return data;
    }

    public Map<String, Object> refresh(Long userId) {
        // 旧 token 由控制器在白名单内解析校验，传入 userId
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRealName(), roles);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expiresAt", LocalDateTime.now().plusSeconds(jwtUtils.getExpireSeconds())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return data;
    }

    @Transactional
    public void changePassword(String oldPwd, String newPwd) {
        Long userId = com.demo.utils.UserContext.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (!passwordUtils.matches(oldPwd, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        sysUserMapper.updatePassword(userId, passwordUtils.encode(newPwd), user.getUsername());
    }

    public void logout() {
        // 无状态 JWT：前端清除即可。此处可扩展加入黑名单。
    }
}
