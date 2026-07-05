package com.demo.controller.business;

import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.Equipment;
import com.demo.security.RequirePermission;
import com.demo.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    private Map<String, Object> buildParams(int page, int size, Long communityId, Integer category, String name,
                                            String code, Integer status, Integer onlineStatus) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("communityId", communityId);
        p.put("category", category);
        p.put("name", name);
        p.put("code", code);
        p.put("status", status);
        p.put("onlineStatus", onlineStatus);
        return p;
    }

    @GetMapping
    @RequirePermission("business:equipment:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) Integer category,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(required = false) String code,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) Integer onlineStatus) {
        return Result.success(equipmentService.page(buildParams(page, size, communityId, category, name, code, status, onlineStatus)));
    }

    @GetMapping("/expiring")
    @RequirePermission("business:equipment:list")
    public Result<Map<String, Object>> expiring(@RequestParam(required = false) Long communityId,
                                                @RequestParam(required = false) String type) {
        return Result.success(equipmentService.expiring(communityId, type));
    }

    @GetMapping("/{id}")
    @RequirePermission("business:equipment:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(equipmentService.detail(id));
    }

    @PostMapping
    @RequirePermission("business:equipment:add")
    public Result<Map<String, Object>> create(@RequestBody Equipment e) {
        Long id = equipmentService.create(e);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("business:equipment:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Equipment e) {
        equipmentService.update(id, e);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("business:equipment:delete")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/check")
    @RequirePermission("business:equipment:check")
    public Result<Map<String, Object>> check(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int result = ((Number) body.get("result")).intValue();
        String issueDesc = (String) body.get("issueDesc");
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) body.get("images");
        String next = (String) body.get("nextCheckDate");
        java.time.LocalDate nextDate = next == null ? null : java.time.LocalDate.parse(next);
        Long checkId = equipmentService.check(id, result, issueDesc, images, nextDate);
        Map<String, Object> data = new HashMap<>();
        data.put("checkId", checkId);
        return Result.success("巡检已提交", data);
    }

    @GetMapping("/{id}/checks")
    @RequirePermission("business:equipment:query")
    public Result<PageResult<Map<String, Object>>> checks(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(equipmentService.checks(id, page, size));
    }
}
