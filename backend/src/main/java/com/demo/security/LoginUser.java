package com.demo.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 解析 JWT 后放入 ThreadLocal 的当前登录用户
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
    private Long communityId;
    private Integer userType;
}
