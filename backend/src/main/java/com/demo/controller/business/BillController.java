package com.demo.controller.business;

import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.security.RequirePermission;
import com.demo.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/business/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    private Map<String, Object> buildParams(int page, int size, Long communityId, Long buildingId, Long houseId,
                                            Long ownerId, Integer feeItemType, Integer status, String period, Integer overdue) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("communityId", communityId);
        p.put("buildingId", buildingId);
        p.put("houseId", houseId);
        p.put("ownerId", ownerId);
        p.put("feeItemType", feeItemType);
        p.put("status", status);
        p.put("period", period);
        p.put("overdue", overdue);
        return p;
    }

    @GetMapping
    @RequirePermission("business:bill:list")
    public Result<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) Long buildingId,
                                                        @RequestParam(required = false) Long houseId,
                                                        @RequestParam(required = false) Long ownerId,
                                                        @RequestParam(required = false) Integer feeItemType,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(required = false) String period,
                                                        @RequestParam(required = false) Integer overdue) {
        return Result.success(billService.page(buildParams(page, size, communityId, buildingId, houseId, ownerId, feeItemType, status, period, overdue)));
    }

    @GetMapping("/{id}")
    @RequirePermission("business:bill:query")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(billService.detail(id));
    }

    @PostMapping("/generate")
    @RequirePermission("business:bill:generate")
    public Result<Map<String, Object>> generate(@RequestBody Map<String, Object> body) {
        Long communityId = longOf(body.get("communityId"));
        Long buildingId = longOf(body.get("buildingId"));
        Long feeItemId = longOf(body.get("feeItemId"));
        String period = (String) body.get("period");
        LocalDate dueDate = body.get("dueDate") == null ? null : LocalDate.parse(body.get("dueDate").toString());
        String remark = (String) body.get("remark");
        return Result.success("生成完成", billService.generate(communityId, buildingId, feeItemId, period, dueDate, remark));
    }

    @PostMapping("/{id}/pay")
    @RequirePermission("business:bill:pay")
    public Result<Map<String, Object>> pay(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        int payMethod = ((Number) body.get("payMethod")).intValue();
        String remark = (String) body.get("remark");
        return Result.success("缴费成功", billService.pay(id, amount, payMethod, remark));
    }

    @PostMapping("/{id}/void")
    @RequirePermission("business:bill:void")
    public Result<Void> voidBill(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        billService.voidBill(id, (String) body.get("reason"));
        return Result.success("已作废", null);
    }

    @GetMapping("/export")
    @RequirePermission("business:bill:export")
    public void export(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10000") int size,
                       @RequestParam(required = false) Long communityId,
                       @RequestParam(required = false) Long buildingId,
                       @RequestParam(required = false) Long houseId,
                       @RequestParam(required = false) Long ownerId,
                       @RequestParam(required = false) Integer feeItemType,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String period,
                       @RequestParam(required = false) Integer overdue,
                       HttpServletResponse response) {
        billService.export(buildParams(page, size, communityId, buildingId, houseId, ownerId, feeItemType, status, period, overdue), response);
    }

    private Long longOf(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
