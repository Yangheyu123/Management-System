package com.demo.controller.basedata;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.Building;
import com.demo.security.RequirePermission;
import com.demo.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/basedata/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @RequirePermission("basedata:building:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(required = false) String buildingNo) {
        return Result.success(buildingService.page(page, size, communityId, name, buildingNo));
    }

    @GetMapping("/{id}")
    @RequirePermission("basedata:building:query")
    public Result<Building> detail(@PathVariable Long id) {
        return Result.success(buildingService.detail(id));
    }

    @PostMapping
    @RequirePermission("basedata:building:add")
    public Result<Map<String, Object>> create(@RequestBody Building b) {
        Long id = buildingService.create(b);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("basedata:building:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Building b) {
        buildingService.update(id, b);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("basedata:building:delete")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.success("删除成功", null);
    }
}
