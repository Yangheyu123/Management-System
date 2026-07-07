package com.demo.controller.stat;

import com.demo.common.Result;
import com.demo.security.RequirePermission;
import com.demo.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/dashboard")
    @RequirePermission("stat:dashboard:view")
    public Result<Map<String, Object>> dashboard(@RequestParam(required = false) Long communityId) {
        return Result.success(statService.dashboard(communityId));
    }

    private Map<String, Object> dateParams(Long communityId, Long buildingId, Integer feeItemType, String period,
                                           String startDate, String endDate) {
        Map<String, Object> p = new HashMap<>();
        p.put("communityId", communityId);
        p.put("buildingId", buildingId);
        p.put("feeItemType", feeItemType);
        p.put("period", (period == null || period.isEmpty()) ? null : period);
        if (hasText(startDate)) p.put("startDate", LocalDate.parse(startDate).atStartOfDay());
        if (hasText(endDate)) p.put("endDate", LocalDate.parse(endDate).atStartOfDay());
        return p;
    }

    /** 判断字符串非空（null 或空串均视为无效，避免 LocalDate.parse("") 抛异常） */
    private boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }

    @GetMapping("/charge/summary")
    @RequirePermission("stat:charge:view")
    public Result<Map<String, Object>> chargeSummary(@RequestParam(required = false) Long communityId,
                                                     @RequestParam(required = false) Long buildingId,
                                                     @RequestParam(required = false) Integer feeItemType,
                                                     @RequestParam(required = false) String period,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        return Result.success(statService.chargeSummary(dateParams(communityId, buildingId, feeItemType, period, startDate, endDate)));
    }

    @GetMapping("/charge/trend")
    @RequirePermission("stat:charge:view")
    public Result<Map<String, Object>> chargeTrend(@RequestParam(required = false) Long communityId,
                                                   @RequestParam(defaultValue = "6") int months,
                                                   @RequestParam(required = false) Integer feeItemType) {
        return Result.success(statService.chargeTrend(communityId, months, feeItemType));
    }

    @GetMapping("/charge/by-type")
    @RequirePermission("stat:charge:view")
    public Result<List<Map<String, Object>>> chargeByType(@RequestParam(required = false) Long communityId,
                                                          @RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate) {
        Map<String, Object> p = new HashMap<>();
        p.put("communityId", communityId);
        if (hasText(startDate)) p.put("startDate", LocalDate.parse(startDate).atStartOfDay());
        if (hasText(endDate)) p.put("endDate", LocalDate.parse(endDate).atStartOfDay());
        return Result.success(statService.chargeByType(p));
    }

    @GetMapping("/workorder/summary")
    @RequirePermission("stat:workorder:view")
    public Result<Map<String, Object>> workorderSummary(@RequestParam(required = false) Long communityId,
                                                        @RequestParam(required = false) String startDate,
                                                        @RequestParam(required = false) String endDate) {
        return Result.success(statService.workorderSummary(dateParams(communityId, null, null, null, startDate, endDate)));
    }

    @GetMapping("/workorder/by-status")
    @RequirePermission("stat:workorder:view")
    public Result<List<Map<String, Object>>> workorderByStatus(@RequestParam(required = false) Long communityId) {
        return Result.success(statService.workorderByStatus(communityId));
    }

    @GetMapping("/workorder/by-handler")
    @RequirePermission("stat:workorder:view")
    public Result<List<Map<String, Object>>> workorderByHandler(@RequestParam(required = false) Long communityId,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate) {
        return Result.success(statService.workorderByHandler(dateParams(communityId, null, null, null, startDate, endDate)));
    }

    @GetMapping("/parking/usage")
    @RequirePermission("stat:parking:view")
    public Result<Map<String, Object>> parkingUsage(@RequestParam(required = false) Long communityId) {
        return Result.success(statService.parkingUsage(communityId));
    }

    @GetMapping("/equipment/status")
    @RequirePermission("stat:equipment:view")
    public Result<List<Map<String, Object>>> equipmentStatus(@RequestParam(required = false) Long communityId) {
        return Result.success(statService.equipmentStatus(communityId));
    }
}
