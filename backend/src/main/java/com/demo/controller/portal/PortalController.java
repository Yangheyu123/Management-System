package com.demo.controller.portal;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.Owner;
import com.demo.entity.SysUser;
import com.demo.entity.WorkOrder;
import com.demo.exception.BusinessException;
import com.demo.mapper.OwnerMapper;
import com.demo.mapper.SysUserMapper;
import com.demo.service.BillService;
import com.demo.service.OwnerService;
import com.demo.service.WorkOrderService;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业主端接口（/api/portal/**）
 *
 * 演示项目：业主身份通过 当前登录用户 phone → owner 表 phone 匹配解析。
 * 全部接口强制注入当前业主 ownerId，业主无法越权查看他人数据。
 * 鉴权方式：方法内校验 currentOwnerId()，不依赖 RBAC 权限码矩阵。
 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final SysUserMapper sysUserMapper;
    private final OwnerMapper ownerMapper;
    private final OwnerService ownerService;
    private final WorkOrderService workOrderService;
    private final BillService billService;

    /**
     * 解析当前登录业主的 ownerId。
     * 用 当前用户 phone 匹配 owner 表（演示项目简化方案）。
     * @return 业主 id；非业主账号返回 null
     */
    private Long currentOwnerId() {
        Long userId = UserContext.getUserId();
        if (userId == null) return null;
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null || u.getUserType() == null || u.getUserType() != 2) return null;
        Owner o = ownerMapper.selectByPhone(u.getPhone());
        return o == null ? null : o.getId();
    }

    /** 校验当前用户是业主，返回 ownerId，否则抛业务异常 */
    private Long requireOwner() {
        Long oid = currentOwnerId();
        if (oid == null) throw new BusinessException(403, "非业主账号，无权访问业主端");
        return oid;
    }

    /** 构造带分页参数的 Map（含 page/size/offset，Service/Mapper 需要 offset） */
    private Map<String, Object> pageParams(Long ownerId, int page, int size) {
        Map<String, Object> p = new HashMap<>();
        p.put("ownerId", ownerId);
        p.put("page", page);
        p.put("size", size);
        p.put("offset", (page - 1) * size);
        return p;
    }

    /* ============================== 业主看板 ============================== */

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Long oid = requireOwner();
        Owner o = ownerMapper.selectById(oid);
        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", oid);
        data.put("ownerName", o != null ? o.getName() : "");
        data.put("phone", o != null ? o.getPhone() : "");
        // 房屋数
        List<Map<String, Object>> houses = ownerService.housesOf(oid);
        data.put("houseCount", houses.size());
        data.put("houses", houses);
        // 账单统计：查业主全部账单（一次取大页），Java 层统计未缴
        Map<String, Object> bp = pageParams(oid, 1, 200);
        PageResult<Map<String, Object>> bills = billService.page(bp);
        int unpaidCnt = 0;
        BigDecimal unpaidAmt = BigDecimal.ZERO;
        for (Map<String, Object> r : bills.getList()) {
            int st = toInt(r.get("status"));
            if (st == 1 || st == 2) {
                unpaidCnt++;
                Object u = r.get("unpaidAmount");
                if (u != null) { try { unpaidAmt = unpaidAmt.add(new BigDecimal(String.valueOf(u))); } catch (Exception ignore) {} }
            }
        }
        data.put("unpaidBillCount", unpaidCnt);
        data.put("unpaidAmount", unpaidAmt);
        // 我的报修统计
        Map<String, Object> wp = pageParams(oid, 1, 200);
        PageResult<Map<String, Object>> wos = workOrderService.page(wp);
        data.put("workorderTotal", wos.getTotal());
        data.put("workorderPending", countByStatus(wos.getList(), 1));
        data.put("workorderHandling", countByStatus(wos.getList(), 3));
        return Result.success(data);
    }

    /* ============================== 我的房屋 ============================== */

    @GetMapping("/houses")
    public Result<List<Map<String, Object>>> houses() {
        Long oid = requireOwner();
        return Result.success(ownerService.housesOf(oid));
    }

    /* ============================== 我的报修 ============================== */

    @GetMapping("/workorders")
    public Result<PageResult<Map<String, Object>>> workorders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        Long oid = requireOwner();
        Map<String, Object> p = pageParams(oid, page, size);
        if (status != null) p.put("status", status);
        if (type != null) p.put("type", type);
        return Result.success(workOrderService.page(p));
    }

    @PostMapping("/workorders")
    public Result<Map<String, Object>> createWorkorder(@RequestBody Map<String, Object> body) {
        Long oid = requireOwner();
        WorkOrder w = new WorkOrder();
        w.setOwnerId(oid);                 // 强制绑定当前业主，忽略前端传入
        w.setCommunityId(toLong(body.get("communityId")));
        w.setHouseId(toLong(body.get("houseId")));
        w.setTitle((String) body.get("title"));
        Integer t = toInt(body.get("type"));
        if (t != null) w.setType(t);
        Integer pri = toInt(body.get("priority"));
        w.setPriority(pri != null ? pri : 2);
        w.setDescription((String) body.get("description"));
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) body.get("images");
        return Result.success(workOrderService.create(w, images));
    }

    @PostMapping("/workorders/{id}/rate")
    public Result<Void> rate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long oid = requireOwner();
        // 校验工单归属当前业主
        Map<String, Object> wo = workOrderService.detail(id);
        if (!String.valueOf(oid).equals(String.valueOf(wo.get("ownerId")))) {
            throw new BusinessException(403, "无权评价他人的工单");
        }
        Integer rating = toInt(body.get("rating"));
        if (rating == null || rating < 1 || rating > 5) throw new BusinessException(400, "评分需为 1-5");
        String comment = (String) body.get("ratingComment");
        workOrderService.rate(id, rating, comment);
        return Result.success(null);
    }

    /* ============================== 我的账单 ============================== */

    @GetMapping("/bills")
    public Result<PageResult<Map<String, Object>>> bills(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Long oid = requireOwner();
        Map<String, Object> p = pageParams(oid, page, size);
        if (status != null) p.put("status", status);
        return Result.success(billService.page(p));
    }

    @PostMapping("/bills/{id}/pay")
    public Result<Map<String, Object>> pay(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long oid = requireOwner();
        // 校验账单归属当前业主
        Map<String, Object> bill = billService.detail(id);
        if (!String.valueOf(oid).equals(String.valueOf(bill.get("ownerId")))) {
            throw new BusinessException(403, "无权为他人的账单缴费");
        }
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        Integer payMethod = toInt(body.get("payMethod"));
        if (payMethod == null) throw new BusinessException(400, "请选择支付方式");
        String remark = (String) body.get("remark");
        return Result.success(billService.pay(id, amount, payMethod, remark));
    }

    /* ============================== 工具方法 ============================== */

    private Long toLong(Object o) {
        if (o == null) return null;
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        try { return Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    /** 按工单状态计数 */
    private int countByStatus(List<Map<String, Object>> records, int status) {
        int n = 0;
        if (records == null) return 0;
        for (Map<String, Object> r : records) {
            Object s = r.get("status");
            if (s != null && String.valueOf(status).equals(String.valueOf(s))) n++;
        }
        return n;
    }
}
