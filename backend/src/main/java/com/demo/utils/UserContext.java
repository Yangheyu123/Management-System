package com.demo.utils;

import com.demo.security.LoginUser;

/**
 * 当前登录用户 ThreadLocal 容器
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = get();
        return u == null ? null : u.getUserId();
    }

    public static String getUsername() {
        LoginUser u = get();
        return u == null ? "system" : u.getUsername();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
