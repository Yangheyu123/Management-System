package com.demo.security;

import com.demo.common.Result;
import com.demo.common.ResultCode;
import com.demo.entity.SysUser;
import com.demo.mapper.SysUserMapper;
import com.demo.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鉴权辅助：解析 token、加载用户角色/权限、构造 LoginUser、白名单判断。
 * 抽出来供拦截器复用。
 */
@Component
@RequiredArgsConstructor
public class CurrentUserHolder {

    public static final List<String> WHITELIST = new ArrayList<>();
    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final ObjectMapper OM = new ObjectMapper();

    static {
        WHITELIST.add("/api/auth/login");
        WHITELIST.add("/api/auth/refresh");
        WHITELIST.add("/api/captcha");
    }

    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;

    public boolean isWhitelisted(String path) {
        for (String p : WHITELIST) {
            if (MATCHER.match(p, path)) return true;
        }
        return false;
    }

    /** 解析 token，返回 LoginUser；失败返回 null */
    public LoginUser resolve(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7).trim();
        Claims claims;
        try {
            claims = jwtUtils.parse(token);
        } catch (Exception e) {
            return null;
        }
        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            try { userId = Long.parseLong(claims.getSubject()); } catch (Exception ignore) { return null; }
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1 || user.getStatus() == 0) return null;

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles", List.class);
        if (roles == null) roles = new ArrayList<>();

        List<String> perms;
        if (roles.contains("SUPER_ADMIN")) {
            perms = new ArrayList<>();
            perms.add("*");
        } else {
            perms = sysUserMapper.selectPermissionsByUserId(userId);
        }
        return LoginUser.builder()
                .userId(userId)
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(perms)
                .communityId(user.getCommunityId())
                .userType(user.getUserType())
                .build();
    }

    public boolean hasPermission(LoginUser user, String code) {
        if (user == null || code == null) return false;
        if (user.getPermissions() != null && user.getPermissions().contains("*")) return true;
        return user.getPermissions() != null && user.getPermissions().contains(code);
    }

    public String writeJson(Result<?> result) {
        try { return OM.writeValueAsString(result); } catch (Exception e) { return "{}"; }
    }

    public Date expiryFromNow() {
        return new Date(System.currentTimeMillis() + jwtUtils.getExpireSeconds() * 1000);
    }

    public Map<String, Object> expiryMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("expiresAt", expiryFromNow());
        return m;
    }
}
