package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.Owner;
import com.demo.exception.BusinessException;
import com.demo.mapper.OwnerMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerMapper ownerMapper;

    public PageResult<Map<String, Object>> page(int page, int size, String name, String phone, String idCard,
                                                String plateNo, Integer status) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("name", name);
        p.put("phone", phone);
        p.put("idCard", idCard);
        p.put("plateNo", plateNo);
        p.put("status", status);
        long total = ownerMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : ownerMapper.selectPage(p);
        for (Map<String, Object> r : rows) {
            r.put("idCard", Dict.maskIdCard((String) r.get("idCard")));
            r.put("genderName", Dict.name(Dict.GENDER, intOf(r.get("gender"))));
        }
        return new PageResult<>(page, (int) p.get("size"), total, rows);
    }

    public Map<String, Object> detail(Long id) {
        Owner o = ownerMapper.selectById(id);
        if (o == null) throw new BusinessException(ResultCode.NOT_FOUND);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", o.getId());
        data.put("name", o.getName());
        data.put("phone", o.getPhone());
        data.put("idCard", Dict.maskIdCard(o.getIdCard()));
        data.put("idCardRaw", o.getIdCard());
        data.put("gender", o.getGender());
        data.put("genderName", Dict.name(Dict.GENDER, o.getGender()));
        data.put("plateNo", o.getPlateNo());
        data.put("moveInDate", o.getMoveInDate());
        data.put("status", o.getStatus());
        data.put("statusName", Dict.name(Dict.OWNER_STATUS, o.getStatus()));
        data.put("remark", o.getRemark());
        data.put("createTime", o.getCreateTime());
        data.put("houses", ownerMapper.selectHousesOfOwner(id));
        return data;
    }

    @Transactional
    public Long create(Owner o, List<Map<String, Object>> bindings) {
        if (o.getIdCard() != null && !o.getIdCard().isEmpty()
                && ownerMapper.countByIdCard(o.getIdCard(), null) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "身份证已存在");
        }
        if (o.getStatus() == null) o.setStatus(1);
        o.setCreateBy(UserContext.getUsername());
        o.setUpdateBy(UserContext.getUsername());
        ownerMapper.insert(o);
        if (bindings != null && !bindings.isEmpty()) {
            for (Map<String, Object> b : bindings) {
                ownerMapper.addOwnerHouse(o.getId(),
                        ((Number) b.get("houseId")).longValue(),
                        (String) b.get("relation"),
                        b.get("isPrimary") == null ? 0 : ((Number) b.get("isPrimary")).intValue());
            }
        }
        return o.getId();
    }

    @Transactional
    public void update(Long id, Owner o) {
        if (ownerMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (o.getIdCard() != null && !o.getIdCard().isEmpty()
                && ownerMapper.countByIdCard(o.getIdCard(), id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "身份证已存在");
        }
        o.setId(id);
        o.setUpdateBy(UserContext.getUsername());
        ownerMapper.updateById(o);
    }

    @Transactional
    public void delete(Long id) {
        if (ownerMapper.countUnfinishedBills(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该业主存在未结清账单，不可删除");
        }
        ownerMapper.clearOwnerHouses(id);
        ownerMapper.logicDelete(id, UserContext.getUsername());
    }

    public List<Map<String, Object>> housesOf(Long id) {
        if (ownerMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return ownerMapper.selectHousesOfOwner(id);
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
