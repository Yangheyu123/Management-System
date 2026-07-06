package com.demo.controller.business;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.FeeItem;
import com.demo.security.RequirePermission;
import com.demo.service.FeeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/business/fee-items")
@RequiredArgsConstructor
public class FeeItemController {

    private final FeeItemService feeItemService;

    @GetMapping
    @RequirePermission("business:fee:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(required = false) Integer type,
                                                        @RequestParam(required = false) Integer status) {
        return Result.success(feeItemService.page(page, size, name, type, status));
    }

    @PostMapping
    @RequirePermission("business:fee:add")
    public Result<Map<String, Object>> create(@RequestBody FeeItem f) {
        Long id = feeItemService.create(f);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("business:fee:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody FeeItem f) {
        feeItemService.update(id, f);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("business:fee:delete")
    public Result<Void> delete(@PathVariable Long id) {
        feeItemService.delete(id);
        return Result.success("删除成功", null);
    }
}
