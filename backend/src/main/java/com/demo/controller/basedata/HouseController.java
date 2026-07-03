package com.demo.controller.basedata;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.House;
import com.demo.security.RequirePermission;
import com.demo.service.HouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/basedata/houses")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    @GetMapping
    @RequirePermission("basedata:house:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) Long buildingId,
                                                        @RequestParam(required = false) String houseNo,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) Integer hasOwner) {
        return Result.success(houseService.page(page, size, communityId, buildingId, houseNo, status, hasOwner));
    }

    @GetMapping("/{id}")
    @RequirePermission("basedata:house:query")
    public Result<House> detail(@PathVariable Long id) {
        return Result.success(houseService.detail(id));
    }

    @PostMapping
    @RequirePermission("basedata:house:add")
    public Result<Map<String, Object>> create(@RequestBody House h) {
        Long id = houseService.create(h);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("basedata:house:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody House h) {
        houseService.update(id, h);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("basedata:house:delete")
    public Result<Void> delete(@PathVariable Long id) {
        houseService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}/owners")
    @RequirePermission("basedata:house:query")
    public Result<List<Map<String, Object>>> owners(@PathVariable Long id) {
        return Result.success(houseService.ownersOf(id));
    }
}
