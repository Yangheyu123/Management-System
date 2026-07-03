package com.demo.service;

import com.demo.common.Dict;
import com.demo.mapper.StatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatService {

    private final StatMapper statMapper;

    private BigDecimal dec(Map<String, Object> m, String k) {
        Object o = m == null ? null : m.get(k);
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }

    private int integer(Map<String, Object> m, String k) {
        Object o = m == null ? null : m.get(k);
        return o == null ? 0 : ((Number) o).intValue();
    }

    public Map<String, Object> dashboard(Long communityId) {
        long houseTotal = statMapper.houseTotal(communityId);
        long ownerTotal = statMapper.ownerTotal();
        long houseCheckIn = statMapper.houseCheckIn(communityId);
        double checkInRate = houseTotal == 0 ? 0 : Math.round(houseCheckIn * 1000.0 / houseTotal) / 10.0;

        Map<String, Object> monthCharge = statMapper.monthCharge(communityId);
        Map<String, Object> monthReceived = statMapper.monthReceived(communityId);
        BigDecimal receivable = dec(monthCharge, "receivable");
        BigDecimal received = dec(monthReceived, "received");
        BigDecimal unpaid = receivable.subtract(received);
        double collectionRate = receivable.compareTo(BigDecimal.ZERO) == 0 ? 0
                : Math.round(received.doubleValue() * 10000.0 / receivable.doubleValue()) / 100.0;

        Map<String, Object> eqRate = statMapper.equipmentOnlineRate(communityId);
        double onlineRate = eqRate == null ? 0 : number2(eqRate.get("rate"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("houseTotal", houseTotal);
        data.put("ownerTotal", ownerTotal);
        data.put("checkInRate", checkInRate);
        data.put("monthReceivable", receivable);
        data.put("monthReceived", received);
        data.put("monthUnpaid", unpaid);
        data.put("collectionRate", collectionRate);
        data.put("workorderPending", statMapper.workorderPending());
        data.put("workorderMonthTotal", statMapper.countThisMonth(communityId));
        data.put("equipmentOnlineRate", onlineRate);

        List<Map<String, Object>> todoList = new ArrayList<>();
        long overdue = statMapper.overdueBillCount(communityId);
        if (overdue > 0) todoList.add(todo("overdue_bill", (int) overdue, overdue + " 笔账单已逾期"));
        long woPending = statMapper.workorderPending();
        if (woPending > 0) todoList.add(todo("workorder", (int) woPending, woPending + " 个工单待处理"));
        long eqExp = statMapper.equipmentExpiring(communityId);
        if (eqExp > 0) todoList.add(todo("equipment", (int) eqExp, eqExp + " 台设备待巡检/将过保"));
        data.put("todoList", todoList);
        return data;
    }

    private Map<String, Object> todo(String type, int count, String desc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("count", count);
        m.put("desc", desc);
        return m;
    }

    public Map<String, Object> chargeSummary(Map<String, Object> params) {
        Map<String, Object> s = statMapper.chargeSummary(params);
        BigDecimal receivable = dec(s, "receivable");
        BigDecimal received = dec(s, "received");
        BigDecimal unpaid = receivable.subtract(received);
        double rate = receivable.compareTo(BigDecimal.ZERO) == 0 ? 0
                : Math.round(received.doubleValue() * 10000.0 / receivable.doubleValue()) / 100.0;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("receivable", receivable);
        data.put("received", received);
        data.put("unpaid", unpaid);
        data.put("collectionRate", rate);
        data.put("billCount", integer(s, "billCount"));
        data.put("paidCount", integer(s, "paidCount"));
        data.put("overdueCount", integer(s, "overdueCount"));
        return data;
    }

    public Map<String, Object> chargeTrend(Long communityId, int months, Integer feeItemType) {
        List<String> categories = new ArrayList<>();
        List<BigDecimal> receivable = new ArrayList<>();
        List<BigDecimal> received = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            categories.add(now.minusMonths(i).toString());
            receivable.add(BigDecimal.ZERO);
            received.add(BigDecimal.ZERO);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("months", categories);
        params.put("communityId", communityId);
        params.put("feeItemType", feeItemType);
        List<Map<String, Object>> rows = statMapper.chargeTrend(params);
        Map<String, Integer> idxMap = new HashMap<>();
        for (int i = 0; i < categories.size(); i++) idxMap.put(categories.get(i), i);
        for (Map<String, Object> r : rows) {
            Integer idx = idxMap.get(r.get("period"));
            if (idx == null) continue;
            receivable.set(idx, dec(r, "receivable"));
            received.set(idx, dec(r, "received"));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", categories);
        data.put("receivable", receivable);
        data.put("received", received);
        return data;
    }

    public List<Map<String, Object>> chargeByType(Map<String, Object> params) {
        List<Map<String, Object>> rows = statMapper.chargeByType(params);
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> r : rows) total = total.add(dec(r, "value"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            BigDecimal v = dec(r, "value");
            double pct = total.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : Math.round(v.doubleValue() * 1000.0 / total.doubleValue()) / 10.0;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", r.get("name"));
            m.put("value", v);
            m.put("percent", pct);
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> workorderSummary(Map<String, Object> params) {
        Map<String, Object> s = statMapper.workorderSummary(params);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", integer(s, "total"));
        data.put("finished", integer(s, "finished"));
        data.put("handling", integer(s, "handling"));
        data.put("pending", integer(s, "pending"));
        data.put("canceled", integer(s, "canceled"));
        data.put("avgHandleHours", numberOrNull(s.get("avgHandleHours")));
        data.put("avgRating", numberOrNull(s.get("avgRating")));
        return data;
    }

    public List<Map<String, Object>> workorderByStatus(Long communityId) {
        List<Map<String, Object>> rows = statMapper.workorderByStatus(communityId);
        // 补齐所有状态（1-6）
        Map<Integer, Integer> map = new HashMap<>();
        for (Map<String, Object> r : rows) map.put(((Number) r.get("status")).intValue(), ((Number) r.get("count")).intValue());
        List<Map<String, Object>> out = new ArrayList<>();
        for (int st = 1; st <= 6; st++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", st);
            m.put("name", Dict.name(Dict.WO_STATUS, st));
            m.put("count", map.getOrDefault(st, 0));
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> workorderByHandler(Map<String, Object> params) {
        List<Map<String, Object>> rows = statMapper.workorderByHandler(params);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("handlerId", r.get("handlerId"));
            m.put("handlerName", r.get("handlerName"));
            m.put("finishedCount", r.get("finishedCount"));
            m.put("avgHandleHours", numberOrNull(r.get("avgHandleHours")));
            m.put("avgRating", numberOrNull(r.get("avgRating")));
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> parkingUsage(Long communityId) {
        Map<String, Object> s = statMapper.parkingSummary(communityId);
        int total = integer(s, "total");
        int inUse = integer(s, "inUse");
        int free = integer(s, "free");
        int sold = integer(s, "sold");
        double rate = total == 0 ? 0 : Math.round((inUse + sold) * 1000.0 / total) / 10.0;
        List<Map<String, Object>> byArea = new ArrayList<>();
        for (Map<String, Object> r : statMapper.parkingByArea(communityId)) {
            int t = ((Number) r.get("total")).intValue();
            int u = integer(r, "inUse");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("areaType", r.get("areaType"));
            m.put("areaName", Dict.name(Dict.AREA_TYPE, ((Number) r.get("areaType")).intValue()));
            m.put("total", t);
            m.put("inUse", u);
            m.put("usageRate", t == 0 ? 0 : Math.round(u * 1000.0 / t) / 10.0);
            byArea.add(m);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("inUse", inUse);
        data.put("free", free);
        data.put("sold", sold);
        data.put("usageRate", rate);
        data.put("byArea", byArea);
        return data;
    }

    public List<Map<String, Object>> equipmentStatus(Long communityId) {
        List<Map<String, Object>> rows = statMapper.equipmentByCategory(communityId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            int cat = ((Number) r.get("category")).intValue();
            int total = integer(r, "total");
            int online = integer(r, "online");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", cat);
            m.put("categoryName", Dict.name(Dict.EQUIP_CATEGORY, cat));
            m.put("total", total);
            m.put("normal", integer(r, "normal"));
            m.put("fault", integer(r, "fault"));
            m.put("repairing", integer(r, "repairing"));
            m.put("scrapped", integer(r, "scrapped"));
            m.put("onlineRate", total == 0 ? 0 : Math.round(online * 1000.0 / total) / 10.0);
            out.add(m);
        }
        return out;
    }

    private double number2(Object o) {
        if (o == null) return 0;
        if (o instanceof BigDecimal) return ((BigDecimal) o).doubleValue();
        return Double.parseDouble(o.toString());
    }

    private Object numberOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal) {
            BigDecimal b = (BigDecimal) o;
            return b.compareTo(BigDecimal.ZERO) == 0 ? null : b;
        }
        String s = o.toString();
        return "0".equals(s) || "0.0".equals(s) ? null : new BigDecimal(s);
    }
}
