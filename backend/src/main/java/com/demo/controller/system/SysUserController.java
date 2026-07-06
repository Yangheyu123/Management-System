package com.demo.controller.system;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.SysUser;
import com.demo.security.RequirePermission;
import com.demo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    private Map<String, Object> params(int page, int size, String username, String realName, String phone,
                                       Integer userType, Long communityId, Integer status) {
        Map<String, Object> p = new HashMap<>();
        p.put("username", username);
        p.put("realName", realName);
        p.put("phone", phone);
        p.put("userType", userType);
        p.put("communityId", communityId);
        p.put("status", status);
        int s = size < 1 ? 10 : Math.min(size, 100);
        p.put("offset", (Math.max(page, 1) - 1) * s);
        p.put("size", s);
        p.put("page", Math.max(page, 1));
        return p;
    }

    @GetMapping
    @RequirePermission("system:user:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String username,
                                                        @RequestParam(required = false) String realName,
                                                        @RequestParam(required = false) String phone,
                                                        @RequestParam(required = false) Integer userType,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) Integer status) {
        return Result.success(sysUserService.page(params(page, size, username, realName, phone, userType, communityId, status)));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(sysUserService.detail(id));
    }

    @PostMapping
    @RequirePermission("system:user:add")
    public Result<Map<String, Object>> create(@RequestBody SysUser user) {
        Long id = sysUserService.create(user, user.getRoleIds());
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        sysUserService.update(id, user);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("system:user:reset")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        sysUserService.resetPassword(id, (String) body.get("newPassword"));
        return Result.success("密码已重置", null);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:edit")
    public Result<Void> status(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        sysUserService.updateStatus(id, ((Number) body.get("status")).intValue());
        return Result.success("操作成功", null);
    }

    @GetMapping("/{id}/roles")
    @RequirePermission("system:user:query")
    public Result<Map<String, Object>> roles(@PathVariable Long id) {
        return Result.success(sysUserService.rolesOf(id));
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("system:user:assign")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        sysUserService.assignRoles(id, toLongList(body.get("roleIds")));
        return Result.success("分配成功", null);
    }

    @SuppressWarnings("unchecked")
    private List<Long> toLongList(Object o) {
        if (o == null) return null;
        List<Long> list = new java.util.ArrayList<>();
        for (Object item : (List<Object>) o) list.add(((Number) item).longValue());
        return list;
    }
}
