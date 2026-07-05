package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.Equipment;
import com.demo.entity.EquipmentCheck;
import com.demo.exception.BusinessException;
import com.demo.mapper.EquipmentMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;

    public PageResult<Map<String, Object>> page(Map<String, Object> p) {
        long total = equipmentMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : equipmentMapper.selectPage(p);
        for (Map<String, Object> r : rows) decorate(r);
        return new PageResult<>((int) p.get("page"), (int) p.get("size"), total, rows);
    }

    private void decorate(Map<String, Object> r) {
        r.put("categoryName", Dict.name(Dict.EQUIP_CATEGORY, intOf(r.get("category"))));
        r.put("statusName", Dict.name(Dict.EQUIP_STATUS, intOf(r.get("status"))));
        r.put("onlineStatusName", Dict.name(Dict.ONLINE, intOf(r.get("onlineStatus"))));
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> r = equipmentMapper.selectDetail(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
        decorate(r);
        return r;
    }

    @Transactional
    public Long create(Equipment e) {
        if (equipmentMapper.countByCode(e.getCommunityId(), e.getCategory(), e.getCode(), null) > 0) {
            throw new BusinessException(ResultCode.EQUIPMENT_CODE_EXISTS);
        }
        if (e.getOnlineStatus() == null) e.setOnlineStatus(1);
        if (e.getStatus() == null) e.setStatus(1);
        e.setCreateBy(UserContext.getUsername());
        e.setUpdateBy(UserContext.getUsername());
        equipmentMapper.insert(e);
        return e.getId();
    }

    @Transactional
    public void update(Long id, Equipment e) {
        if (equipmentMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (e.getCommunityId() != null && e.getCategory() != null && e.getCode() != null
                && equipmentMapper.countByCode(e.getCommunityId(), e.getCategory(), e.getCode(), id) > 0) {
            throw new BusinessException(ResultCode.EQUIPMENT_CODE_EXISTS);
        }
        e.setId(id);
        e.setUpdateBy(UserContext.getUsername());
        equipmentMapper.updateById(e);
    }

    @Transactional
    public void delete(Long id) {
        if (equipmentMapper.countChecks(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "设备有巡检记录，建议改为「报废」状态而非删除");
        }
        equipmentMapper.logicDelete(id, UserContext.getUsername());
    }

    @Transactional
    public Long check(Long id, int result, String issueDesc, List<String> images, LocalDate nextCheckDate) {
        Equipment e = equipmentMapper.selectById(id);
        if (e == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (result == 2 && (issueDesc == null || issueDesc.trim().isEmpty())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "异常结果需填写描述");
        }
        EquipmentCheck c = new EquipmentCheck();
        c.setEquipmentId(id);
        c.setCheckerId(UserContext.getUserId());
        c.setCheckTime(java.time.LocalDateTime.now());
        c.setResult(result);
        c.setIssueDesc(issueDesc);
        if (images != null && !images.isEmpty()) c.setImages(String.join(",", images));
        equipmentMapper.insertCheck(c);
        int newStatus = result == 2 ? 2 : (e.getStatus() != null ? e.getStatus() : 1);
        LocalDate next = nextCheckDate != null ? nextCheckDate : LocalDate.now().plusMonths(1);
        equipmentMapper.updateCheckStatus(id, LocalDate.now(), next, newStatus, UserContext.getUsername());
        return c.getId();
    }

    public PageResult<Map<String, Object>> checks(Long id, int page, int size) {
        Map<String, Object> p = PageParams.of(page, size);
        long total = equipmentMapper.countChecksOf(id);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : equipmentMapper.selectChecks(id, (int) p.get("offset"), (int) p.get("size"));
        for (Map<String, Object> r : rows) r.put("resultName", Dict.name(Dict.CHECK_RESULT, intOf(r.get("result"))));
        return new PageResult<>(page, (int) p.get("size"), total, rows);
    }

    public Map<String, Object> expiring(Long communityId, String type) {
        List<Map<String, Object>> all = equipmentMapper.selectForExpiring(communityId);
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> warrantyExpiring = new ArrayList<>();
        List<Map<String, Object>> checkExpiring = new ArrayList<>();
        List<Map<String, Object>> offline = new ArrayList<>();
        for (Map<String, Object> r : all) {
            r.put("categoryName", Dict.name(Dict.EQUIP_CATEGORY, intOf(r.get("category"))));
            Object wd = r.get("warrantyDate");
            Object nd = r.get("nextCheckDate");
            int online = intOf(r.get("onlineStatus"));
            if (wd != null) {
                LocalDate d = LocalDate.parse(wd.toString());
                long days = java.time.temporal.ChronoUnit.DAYS.between(today, d);
                if (days >= 0 && days <= 30) {
                    r.put("daysLeft", days);
                    warrantyExpiring.add(r);
                }
            }
            if (nd != null) {
                LocalDate d = LocalDate.parse(nd.toString());
                long days = java.time.temporal.ChronoUnit.DAYS.between(today, d);
                if (days <= 7) {
                    r.put("daysLeft", days);
                    checkExpiring.add(r);
                }
            }
            if (online == 0) offline.add(r);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("warrantyExpiring", type == null || "warranty".equals(type) ? warrantyExpiring : Collections.emptyList());
        data.put("checkExpiring", type == null || "check".equals(type) ? checkExpiring : Collections.emptyList());
        data.put("offline", type == null || "offline".equals(type) ? offline : Collections.emptyList());
        return data;
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
