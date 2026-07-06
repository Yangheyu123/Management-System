package com.demo.controller.system;

import com.demo.common.Result;
import com.demo.entity.SysRole;
import com.demo.security.RequirePermission;
import com.demo.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping
    @RequirePermission("system:role:list")
    public Result<Object> list(@RequestParam(required = false) String roleName,
                               @RequestParam(required = false) String roleCode,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) Integer page) {
        Map<String, Object> params = new HashMap<>();
        params.put("roleName", roleName);
        params.put("roleCode", roleCode);
        params.put("status", status);
        boolean pageMode = page != null;
        return Result.success(sysRoleService.list(params, pageMode));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(sysRoleService.detail(id));
    }

    @PostMapping
    @RequirePermission("system:role:add")
    public Result<Map<String, Object>> create(@RequestBody SysRole role) {
        Long id = sysRoleService.create(role);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        sysRoleService.update(id, role);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}/permissions")
    @RequirePermission("system:role:query")
    public Result<Map<String, Object>> permissions(@PathVariable Long id) {
        return Result.success(sysRoleService.permissionsOf(id));
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission("system:role:assign")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) body.get("permissionIds");
        List<Long> permIds = new java.util.ArrayList<>();
        if (raw != null) for (Object o : raw) permIds.add(((Number) o).longValue());
        sysRoleService.assignPermissions(id, permIds);
        return Result.success("分配成功", null);
    }
}
