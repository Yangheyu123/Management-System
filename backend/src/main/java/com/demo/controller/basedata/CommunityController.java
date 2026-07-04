package com.demo.controller.basedata;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.Community;
import com.demo.security.RequirePermission;
import com.demo.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/basedata/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    @RequirePermission("basedata:community:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(required = false) String address,
                                                        @RequestParam(required = false) Integer status) {
        return Result.success(communityService.page(page, size, name, address, status));
    }

    @GetMapping("/all")
    @RequirePermission("basedata:community:list")
    public Result<List<Map<String, Object>>> all() {
        return Result.success(communityService.all());
    }

    @GetMapping("/{id}")
    @RequirePermission("basedata:community:query")
    public Result<Community> detail(@PathVariable Long id) {
        return Result.success(communityService.detail(id));
    }

    @PostMapping
    @RequirePermission("basedata:community:add")
    public Result<Map<String, Object>> create(@RequestBody Community c) {
        Long id = communityService.create(c);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("basedata:community:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Community c) {
        communityService.update(id, c);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("basedata:community:delete")
    public Result<Void> delete(@PathVariable Long id) {
        communityService.delete(id);
        return Result.success("删除成功", null);
    }
}
