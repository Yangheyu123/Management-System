package com.demo.controller.business;

import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.WorkOrder;
import com.demo.security.RequirePermission;
import com.demo.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business/workorders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    private Map<String, Object> params(int page, int size, Long communityId, String orderNo, Long houseId, Long ownerId,
                                       Integer type, Integer priority, Integer status, Long handlerId,
                                       String startDate, String endDate) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("communityId", communityId);
        p.put("orderNo", orderNo);
        p.put("houseId", houseId);
        p.put("ownerId", ownerId);
        p.put("type", type);
        p.put("priority", priority);
        p.put("status", status);
        p.put("handlerId", handlerId);
        if (startDate != null) p.put("startDate", LocalDate.parse(startDate).atStartOfDay());
        if (endDate != null) p.put("endDate", LocalDate.parse(endDate).atStartOfDay());
        return p;
    }

    @GetMapping
    @RequirePermission("business:workorder:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) String orderNo,
                                                        @RequestParam(required = false) Long houseId,
                                                        @RequestParam(required = false) Long ownerId,
                                                        @RequestParam(required = false) Integer type,
                                                        @RequestParam(required = false) Integer priority,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) Long handlerId,
                                                        @RequestParam(required = false) String startDate,
                                                        @RequestParam(required = false) String endDate) {
        return Result.success(workOrderService.page(params(page, size, communityId, orderNo, houseId, ownerId, type, priority, status, handlerId, startDate, endDate)));
    }

    @GetMapping("/{id}")
    @RequirePermission("business:workorder:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(workOrderService.detail(id));
    }

    @PostMapping
    @RequirePermission("business:workorder:add")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        WorkOrder w = new WorkOrder();
        w.setCommunityId(longOf(body.get("communityId")));
        w.setHouseId(longOf(body.get("houseId")));
        w.setOwnerId(longOf(body.get("ownerId")));
        w.setTitle((String) body.get("title"));
        w.setType(intOf(body.get("type")));
        w.setPriority(intOf(body.get("priority")));
        w.setDescription((String) body.get("description"));
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) body.get("images");
        return Result.success("报修成功", workOrderService.create(w, images));
    }

    @PutMapping("/{id}")
    @RequirePermission("business:workorder:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody WorkOrder w) {
        workOrderService.updateBasic(id, w);
        return Result.success("修改成功", null);
    }

    @PostMapping("/{id}/assign")
    @RequirePermission("business:workorder:assign")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        workOrderService.assign(id, longOf(body.get("handlerId")), (String) body.get("remark"));
        return Result.success("派单成功", null);
    }

    @PostMapping("/{id}/accept")
    @RequirePermission("business:workorder:accept")
    public Result<Void> accept(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        workOrderService.accept(id, (String) body.get("remark"));
        return Result.success("接单成功", null);
    }

    @PostMapping("/{id}/finish")
    @RequirePermission("business:workorder:finish")
    public Result<Void> finish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        workOrderService.finish(id, (String) body.get("handleResult"), (String) body.get("remark"));
        return Result.success("处理完成", null);
    }

    @PostMapping("/{id}/close")
    @RequirePermission("business:workorder:close")
    public Result<Void> close(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        workOrderService.close(id, (String) body.get("remark"));
        return Result.success("已关闭", null);
    }

    @PostMapping("/{id}/cancel")
    @RequirePermission("business:workorder:cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        workOrderService.cancel(id, (String) body.get("reason"));
        return Result.success("已撤销", null);
    }

    @PostMapping("/{id}/rate")
    @RequirePermission("business:workorder:rate")
    public Result<Void> rate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        workOrderService.rate(id, ((Number) body.get("rating")).intValue(), (String) body.get("ratingComment"));
        return Result.success("评价成功", null);
    }

    private Long longOf(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
