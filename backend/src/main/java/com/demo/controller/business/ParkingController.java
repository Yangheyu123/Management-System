package com.demo.controller.business;

import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.ParkingSpace;
import com.demo.security.RequirePermission;
import com.demo.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/business/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingSpaceService parkingSpaceService;

    @GetMapping("/spaces")
    @RequirePermission("business:parking:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) String spaceNo,
                                                        @RequestParam(required = false) Integer areaType,
                                                        @RequestParam(required = false) Integer useType,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) Long ownerId,
                                                        @RequestParam(required = false) String plateNo) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("communityId", communityId);
        p.put("spaceNo", spaceNo);
        p.put("areaType", areaType);
        p.put("useType", useType);
        p.put("status", status);
        p.put("ownerId", ownerId);
        p.put("plateNo", plateNo);
        return Result.success(parkingSpaceService.page(p));
    }

    @GetMapping("/spaces/{id}")
    @RequirePermission("business:parking:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(parkingSpaceService.detail(id));
    }

    @PostMapping("/spaces")
    @RequirePermission("business:parking:add")
    public Result<Map<String, Object>> create(@RequestBody ParkingSpace ps) {
        Long id = parkingSpaceService.create(ps);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    @PutMapping("/spaces/{id}")
    @RequirePermission("business:parking:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody ParkingSpace ps) {
        parkingSpaceService.update(id, ps);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/spaces/{id}")
    @RequirePermission("business:parking:delete")
    public Result<Void> delete(@PathVariable Long id) {
        parkingSpaceService.delete(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/spaces/{id}/bind")
    @RequirePermission("business:parking:bind")
    public Result<Void> bind(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        parkingSpaceService.bind(id, longOf(body.get("ownerId")), (String) body.get("plateNo"),
                body.get("startDate") == null ? null : LocalDate.parse(body.get("startDate").toString()),
                body.get("endDate") == null ? null : LocalDate.parse(body.get("endDate").toString()),
                body.get("amount") == null ? null : new java.math.BigDecimal(body.get("amount").toString()),
                (String) body.get("remark"));
        return Result.success("绑定成功", null);
    }

    @PostMapping("/spaces/{id}/unbind")
    @RequirePermission("business:parking:unbind")
    public Result<Void> unbind(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        parkingSpaceService.unbind(id, (String) body.get("remark"));
        return Result.success("已解绑", null);
    }

    @PostMapping("/spaces/{id}/renew")
    @RequirePermission("business:parking:renew")
    public Result<Map<String, Object>> renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int months = ((Number) body.get("months")).intValue();
        java.math.BigDecimal amount = body.get("amount") == null ? null : new java.math.BigDecimal(body.get("amount").toString());
        return Result.success("续费成功", parkingSpaceService.renew(id, months, amount, (String) body.get("remark")));
    }

    @GetMapping("/records")
    @RequirePermission("business:parking:list")
    public Result<PageResult<Map<String, Object>>> records(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(required = false) Long spaceId,
                                                           @RequestParam(required = false) Long ownerId,
                                                           @RequestParam(required = false) String action) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("spaceId", spaceId);
        p.put("ownerId", ownerId);
        p.put("action", action);
        return Result.success(parkingSpaceService.recordsPage(p));
    }

    private Long longOf(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
