package com.demo.config;

import com.demo.entity.SysUser;
import com.demo.mapper.SysUserMapper;
import com.demo.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动时确保种子账号密码为正确的 BCrypt（data.sql 中密码统一写为 'INIT'）。
 * 这样无需手工算哈希，且可重复执行。
 *   admin    / admin123
 *   其余账号 / 123456
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordUtils passwordUtils;

    private static final Map<String, String> DEFAULT_PWD = new HashMap<>();

    static {
        DEFAULT_PWD.put("admin", "admin123");
        DEFAULT_PWD.put("manager01", "123456");
        DEFAULT_PWD.put("toll01", "123456");
        DEFAULT_PWD.put("repair01", "123456");
        DEFAULT_PWD.put("owner01", "123456");
    }

    @Override
    public void run(String... args) {
        int fixed = 0;
        for (Map.Entry<String, String> e : DEFAULT_PWD.entrySet()) {
            SysUser u = sysUserMapper.selectByUsername(e.getKey());
            if (u == null) continue;
            if ("INIT".equals(u.getPassword()) || u.getPassword() == null
                    || !passwordUtils.matches(e.getValue(), u.getPassword())) {
                // 只在仍是占位符或无法匹配默认密码时重置，避免覆盖用户已修改的密码
                if ("INIT".equals(u.getPassword()) || u.getPassword() == null) {
                    sysUserMapper.updatePassword(u.getId(), passwordUtils.encode(e.getValue()), "system");
                    fixed++;
                }
            }
        }
        if (fixed > 0) {
            log.info("DataInitializer: 已为 {} 个种子账号写入 BCrypt 密码", fixed);
        }
    }
}
