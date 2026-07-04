package com.demo.controller.basedata;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.Owner;
import com.demo.security.RequirePermission;
import com.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/basedata/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    @RequirePermission("basedata:owner:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(required = false) String phone,
                                                        @RequestParam(required = false) String idCard,
                                                        @RequestParam(required = false) String plateNo,
                                                        @RequestParam(required = false) Integer status) {
        return Result.success(ownerService.page(page, size, name, phone, idCard, plateNo, status));
    }

    @GetMapping("/{id}")
    @RequirePermission("basedata:owner:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(ownerService.detail(id));
    }

    @PostMapping
    @RequirePermission("basedata:owner:add")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Owner o = bindOwner(body);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bindings = (List<Map<String, Object>>) body.get("houseBindings");
        Long id = ownerService.create(o, bindings);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/{id}")
    @RequirePermission("basedata:owner:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Owner o = bindOwner(body);
        ownerService.update(id, o);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("basedata:owner:delete")
    public Result<Void> delete(@PathVariable Long id) {
        ownerService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}/houses")
    @RequirePermission("basedata:owner:query")
    public Result<List<Map<String, Object>>> houses(@PathVariable Long id) {
        return Result.success(ownerService.housesOf(id));
    }

    private Owner bindOwner(Map<String, Object> body) {
        Owner o = new Owner();
        o.setName((String) body.get("name"));
        o.setPhone((String) body.get("phone"));
        o.setIdCard((String) body.get("idCard"));
        o.setGender(intOf(body.get("gender")));
        o.setPlateNo((String) body.get("plateNo"));
        Object mid = body.get("moveInDate");
        if (mid != null && !"".equals(mid)) o.setMoveInDate(java.time.LocalDate.parse(mid.toString()));
        o.setStatus(intOf(body.get("status")));
        o.setRemark((String) body.get("remark"));
        return o;
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
