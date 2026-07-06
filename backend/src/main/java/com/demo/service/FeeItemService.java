package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.FeeItem;
import com.demo.exception.BusinessException;
import com.demo.mapper.FeeItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeeItemService {

    private final FeeItemMapper feeItemMapper;

    public PageResult<Map<String, Object>> page(int page, int size, String name, Integer type, Integer status) {
        Map<String, Object> p = PageParams.of(page, size);
        p.put("name", name);
        p.put("type", type);
        p.put("status", status);
        long total = feeItemMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? java.util.Collections.emptyList() : feeItemMapper.selectPage(p);
        for (Map<String, Object> r : rows) {
            r.put("typeName", Dict.name(Dict.FEE_TYPE, intOf(r.get("type"))));
            r.put("billingCycleName", Dict.name(Dict.BILLING_CYCLE, intOf(r.get("billingCycle"))));
        }
        return new PageResult<>(page, (int) p.get("size"), total, rows);
    }

    @Transactional
    public Long create(FeeItem f) {
        if (feeItemMapper.countByName(f.getName(), null) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "收费项目名已存在");
        }
        if (f.getStatus() == null) f.setStatus(1);
        if (f.getBillingCycle() == null) f.setBillingCycle(1);
        feeItemMapper.insert(f);
        return f.getId();
    }

    @Transactional
    public void update(Long id, FeeItem f) {
        if (feeItemMapper.selectById(id) == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (f.getName() != null && feeItemMapper.countByName(f.getName(), id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "收费项目名已存在");
        }
        f.setId(id);
        feeItemMapper.updateById(f);
    }

    @Transactional
    public void delete(Long id) {
        if (feeItemMapper.countBills(id) > 0) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "已有账单引用该收费项目，建议停用而非删除");
        }
        feeItemMapper.logicDelete(id);
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
