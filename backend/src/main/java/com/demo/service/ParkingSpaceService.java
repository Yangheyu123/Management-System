package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.ParkingRecord;
import com.demo.entity.ParkingSpace;
import com.demo.exception.BusinessException;
import com.demo.mapper.ParkingRecordMapper;
import com.demo.mapper.ParkingSpaceMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParkingSpaceService {

    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingRecordMapper parkingRecordMapper;

    public PageResult<Map<String, Object>> page(Map<String, Object> p) {
        long total = parkingSpaceMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? java.util.Collections.emptyList() : parkingSpaceMapper.selectPage(p);
        for (Map<String, Object> r : rows) decorate(r);
        return new PageResult<>((int) p.get("page"), (int) p.get("size"), total, rows);
    }

    private void decorate(Map<String, Object> r) {
        r.put("areaTypeName", Dict.name(Dict.AREA_TYPE, intOf(r.get("areaType"))));
        r.put("useTypeName", Dict.name(Dict.USE_TYPE, intOf(r.get("useType"))));
        r.put("statusName", Dict.name(Dict.PARKING_STATUS, intOf(r.get("status"))));
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> r = parkingSpaceMapper.selectDetail(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
        decorate(r);
        return r;
    }

    @Transactional
    public Long create(ParkingSpace ps) {
        if (parkingSpaceMapper.countByNo(ps.getCommunityId(), ps.getSpaceNo(), null) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该小区下车位号已存在");
        }
        if (ps.getStatus() == null) ps.setStatus(1);
        ps.setCreateBy(UserContext.getUsername());
        ps.setUpdateBy(UserContext.getUsername());
        parkingSpaceMapper.insert(ps);
        return ps.getId();
    }

    @Transactional
    public void update(Long id, ParkingSpace ps) {
        if (parkingSpaceMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (ps.getCommunityId() != null && ps.getSpaceNo() != null
                && parkingSpaceMapper.countByNo(ps.getCommunityId(), ps.getSpaceNo(), id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该小区下车位号已存在");
        }
        ps.setId(id);
        ps.setUpdateBy(UserContext.getUsername());
        parkingSpaceMapper.updateById(ps);
    }

    @Transactional
    public void delete(Long id) {
        ParkingSpace ps = parkingSpaceMapper.selectById(id);
        if (ps == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (ps.getStatus() != null && ps.getStatus() != 1) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "车位使用中，不可删除");
        }
        parkingSpaceMapper.logicDelete(id, UserContext.getUsername());
    }

    @Transactional
    public void bind(Long id, Long ownerId, String plateNo, LocalDate start, LocalDate end, java.math.BigDecimal amount, String remark) {
        ParkingSpace ps = parkingSpaceMapper.selectById(id);
        if (ps == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (ps.getStatus() != null && (ps.getStatus() == 2 || ps.getStatus() == 3)) {
            throw new BusinessException(ResultCode.PARKING_OCCUPIED);
        }
        int newStatus = ps.getUseType() != null && ps.getUseType() == 2 ? 3 : 2;
        parkingSpaceMapper.bind(id, ownerId, plateNo, start, end, newStatus, UserContext.getUsername());
        record(id, ownerId, plateNo, "绑定", amount, start, end);
    }

    @Transactional
    public void unbind(Long id, String remark) {
        ParkingSpace ps = parkingSpaceMapper.selectById(id);
        if (ps == null) throw new BusinessException(ResultCode.NOT_FOUND);
        parkingSpaceMapper.unbind(id, UserContext.getUsername());
        record(id, ps.getOwnerId(), ps.getPlateNo(), "解绑", null, null, null);
    }

    @Transactional
    public Map<String, Object> renew(Long id, int months, java.math.BigDecimal amount, String remark) {
        ParkingSpace ps = parkingSpaceMapper.selectById(id);
        if (ps == null) throw new BusinessException(ResultCode.NOT_FOUND);
        java.math.BigDecimal fee = amount != null ? amount
                : (ps.getMonthlyFee() != null ? ps.getMonthlyFee().multiply(java.math.BigDecimal.valueOf(months)) : java.math.BigDecimal.ZERO);
        LocalDate base = ps.getEndDate() != null && ps.getEndDate().isAfter(LocalDate.now()) ? ps.getEndDate() : LocalDate.now();
        LocalDate newEnd = base.plusMonths(months);
        parkingSpaceMapper.renew(id, newEnd, UserContext.getUsername());
        record(id, ps.getOwnerId(), ps.getPlateNo(), "续费", fee, base, newEnd);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("newEndDate", newEnd);
        data.put("amount", fee);
        return data;
    }

    private void record(Long spaceId, Long ownerId, String plateNo, String action, java.math.BigDecimal amount,
                        LocalDate start, LocalDate end) {
        ParkingRecord pr = new ParkingRecord();
        pr.setSpaceId(spaceId);
        pr.setOwnerId(ownerId);
        pr.setPlateNo(plateNo);
        pr.setAction(action);
        pr.setAmount(amount);
        pr.setStartDate(start);
        pr.setEndDate(end);
        pr.setOperatorId(UserContext.getUserId());
        parkingSpaceMapper.insertRecord(pr);
    }

    public PageResult<Map<String, Object>> recordsPage(Map<String, Object> p) {
        long total = parkingRecordMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? java.util.Collections.emptyList() : parkingRecordMapper.selectPage(p);
        return new PageResult<>((int) p.get("page"), (int) p.get("size"), total, rows);
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
