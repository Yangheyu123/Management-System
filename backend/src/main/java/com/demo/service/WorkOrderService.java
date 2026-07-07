package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.WorkOrder;
import com.demo.entity.WorkOrderLog;
import com.demo.exception.BusinessException;
import com.demo.mapper.WorkOrderMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PageResult<Map<String, Object>> page(Map<String, Object> p) {
        long total = workOrderMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : workOrderMapper.selectPage(p);
        for (Map<String, Object> r : rows) decorate(r);
        return new PageResult<>((int) p.get("page"), (int) p.get("size"), total, rows);
    }

    private void decorate(Map<String, Object> r) {
        r.put("typeName", Dict.name(Dict.WO_TYPE, intOf(r.get("type"))));
        r.put("priorityName", Dict.name(Dict.WO_PRIORITY, intOf(r.get("priority"))));
        r.put("statusName", Dict.name(Dict.WO_STATUS, intOf(r.get("status"))));
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> r = workOrderMapper.selectDetail(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
        decorate(r);
        List<WorkOrderLog> logs = workOrderMapper.selectLogs(id);
        List<Map<String, Object>> logList = new ArrayList<>();
        for (WorkOrderLog l : logs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId());
            m.put("orderId", l.getOrderId());
            m.put("operatorName", l.getOperatorName());
            m.put("action", l.getAction());
            m.put("fromStatus", l.getFromStatus());
            m.put("toStatus", l.getToStatus());
            m.put("fromStatusName", Dict.name(Dict.WO_STATUS, l.getFromStatus()));
            m.put("toStatusName", Dict.name(Dict.WO_STATUS, l.getToStatus()));
            m.put("remark", l.getRemark());
            m.put("createTime", l.getCreateTime());
            logList.add(m);
        }
        r.put("logs", logList);
        r.put("images", parseImages(r.get("images")));
        return r;
    }

    @Transactional
    public Map<String, Object> create(WorkOrder w, List<String> images) {
        w.setOrderNo(genOrderNo());
        if (w.getStatus() == null) w.setStatus(1);
        if (w.getPriority() == null) w.setPriority(2);
        w.setCreateBy(UserContext.getUsername());
        w.setUpdateBy(UserContext.getUsername());
        if (images != null && !images.isEmpty()) {
            w.setImages(String.join(",", images));
        }
        workOrderMapper.insert(w);
        log(w.getId(), UserContext.getUserId(), UserContext.get().getRealName(), "创建", null, 1, "业主/用户报修");
        Map<String, Object> data = new HashMap<>();
        data.put("id", w.getId());
        data.put("orderNo", w.getOrderNo());
        return data;
    }

    @Transactional
    public void updateBasic(Long id, WorkOrder w) {
        WorkOrder exists = workOrderMapper.selectById(id);
        if (exists == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (exists.getStatus() != 1) {
            throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL);
        }
        w.setId(id);
        w.setUpdateBy(UserContext.getUsername());
        workOrderMapper.updateBasic(w);
    }

    @Transactional
    public void assign(Long id, Long handlerId, String remark) {
        if (handlerId == null) throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "维修员不能为空");
        if (workOrderMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        int n = workOrderMapper.assign(id, handlerId, UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL);
        log(id, UserContext.getUserId(), UserContext.get().getRealName(), "派单", 1, 2, remark);
    }

    @Transactional
    public void accept(Long id, String remark) {
        if (workOrderMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        int n = workOrderMapper.accept(id, LocalDateTime.now(), UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.WORKORDER_ALREADY_ACCEPTED.getCode(),
                ResultCode.WORKORDER_ALREADY_ACCEPTED.getMessage());
        log(id, UserContext.getUserId(), UserContext.get().getRealName(), "接单", 2, 3, remark);
    }

    @Transactional
    public void finish(Long id, String result, String remark) {
        if (result == null || result.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "处理结果不能为空");
        }
        if (workOrderMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        int n = workOrderMapper.finish(id, result, LocalDateTime.now(), UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL);
        log(id, UserContext.getUserId(), UserContext.get().getRealName(), "处理完成", 3, 4, result);
    }

    @Transactional
    public void close(Long id, String remark) {
        if (workOrderMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        int n = workOrderMapper.close(id, UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL);
        log(id, UserContext.getUserId(), UserContext.get().getRealName(), "关闭", 4, 5, remark);
    }

    @Transactional
    public void cancel(Long id, String reason) {
        WorkOrder w = workOrderMapper.selectById(id);
        if (w == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (w.getStatus() >= 4) throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL);
        int n = workOrderMapper.cancel(id, UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL);
        log(id, UserContext.getUserId(), UserContext.get().getRealName(), "撤销", w.getStatus(), 6, reason);
    }

    @Transactional
    public void rate(Long id, int rating, String comment) {
        WorkOrder w = workOrderMapper.selectById(id);
        if (w == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (w.getStatus() < 4) throw new BusinessException(ResultCode.WORKORDER_STATUS_ILLEGAL.getCode(), "工单未完成，不可评价");
        int n = workOrderMapper.rate(id, rating, comment, UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.CONFLICT.getCode(), "该工单已评价过");
        log(id, UserContext.getUserId(), UserContext.get().getRealName(), "评价", null, null, "评分" + rating + "星");
    }

    private void log(Long orderId, Long opId, String opName, String action, Integer from, Integer to, String remark) {
        WorkOrderLog l = new WorkOrderLog();
        l.setOrderId(orderId);
        l.setOperatorId(opId);
        l.setOperatorName(opName);
        l.setAction(action);
        l.setFromStatus(from);
        l.setToStatus(to);
        l.setRemark(remark);
        workOrderMapper.insertLog(l);
    }

    private String genOrderNo() {
        String prefix = "WO" + LocalDateTime.now().format(DAY);
        long seq = workOrderMapper.countByPeriod(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))) + 1;
        return prefix + String.format("%03d", seq);
    }

    private List<String> parseImages(Object img) {
        if (img == null) return Collections.emptyList();
        String s = img.toString();
        if (s.isEmpty()) return Collections.emptyList();
        return Arrays.asList(s.split(","));
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}

// [fix] 工单状态筛选支持多值并联查询，补充空集合兜底
