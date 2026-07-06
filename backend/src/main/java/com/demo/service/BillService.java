package com.demo.service;

import com.demo.common.Dict;
import com.demo.common.PageParams;
import com.demo.common.PageResult;
import com.demo.common.ResultCode;
import com.demo.entity.Bill;
import com.demo.entity.FeeItem;
import com.demo.entity.Payment;
import com.demo.exception.BusinessException;
import com.demo.mapper.BillMapper;
import com.demo.mapper.FeeItemMapper;
import com.demo.mapper.PaymentMapper;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillMapper billMapper;
    private final FeeItemMapper feeItemMapper;
    private final PaymentMapper paymentMapper;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PageResult<Map<String, Object>> page(Map<String, Object> p) {
        long total = billMapper.countPage(p);
        List<Map<String, Object>> rows = total == 0 ? Collections.emptyList() : billMapper.selectPage(p);
        for (Map<String, Object> r : rows) decorate(r);
        return new PageResult<>((int) p.get("page"), (int) p.get("size"), total, rows);
    }

    private void decorate(Map<String, Object> r) {
        r.put("feeItemTypeName", Dict.name(Dict.FEE_TYPE, intOf(r.get("feeItemType"))));
        r.put("statusName", Dict.name(Dict.BILL_STATUS, intOf(r.get("status"))));
        BigDecimal amount = dec(r.get("amount"));
        BigDecimal paid = dec(r.get("paidAmount"));
        BigDecimal unpaid = amount.subtract(paid);
        r.put("unpaidAmount", unpaid);
        int status = intOf(r.get("status"));
        Object due = r.get("dueDate");
        boolean overdue = (status == 1 || status == 2) && due != null
                && LocalDate.parse(due.toString()).isBefore(LocalDate.now());
        r.put("overdue", overdue ? 1 : 0);
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> r = billMapper.selectDetail(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
        decorate(r);
        List<Map<String, Object>> pays = paymentMapper.selectByBillIds(Collections.singletonList(id));
        for (Map<String, Object> p : pays) {
            p.put("payMethodName", Dict.name(Dict.PAY_METHOD, intOf(p.get("payMethod"))));
        }
        r.put("payments", pays);
        return r;
    }

    @Transactional
    public Map<String, Object> generate(Long communityId, Long buildingId, Long feeItemId, String period,
                                        LocalDate dueDate, String remark) {
        if (communityId == null || feeItemId == null || period == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不全：communityId/feeItemId/period 必填");
        }
        FeeItem item = feeItemMapper.selectById(feeItemId);
        if (item == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "收费项目不存在");

        Map<String, Object> hp = new HashMap<>();
        hp.put("communityId", communityId);
        hp.put("buildingId", buildingId);
        List<Map<String, Object>> houses = billMapper.selectBillableHouses(hp);

        int generated = 0, skipped = 0;
        List<Bill> batch = new ArrayList<>();
        long baseSeq = billMapper.countPage(new HashMap<>()) + 1;
        int idx = 0;
        for (Map<String, Object> h : houses) {
            Long houseId = ((Number) h.get("house_id")).longValue();
            Long ownerId = h.get("owner_id") == null ? null : ((Number) h.get("owner_id")).longValue();
            if (ownerId == null) {
                skipped++;
                continue;
            }
            if (billMapper.countExists(houseId, feeItemId, period) > 0) {
                skipped++;
                continue;
            }
            BigDecimal quantity;
            if (item.getType() != null && item.getType() == 1) {
                quantity = dec(h.get("area"));
            } else {
                quantity = BigDecimal.ONE;
            }
            BigDecimal amount = item.getUnitPrice().multiply(quantity).setScale(2, BigDecimal.ROUND_HALF_UP);

            Bill b = new Bill();
            b.setBillNo("B" + period.replace("-", "") + String.format("%04d", baseSeq + idx));
            b.setCommunityId(communityId);
            b.setHouseId(houseId);
            b.setOwnerId(ownerId);
            b.setFeeItemId(feeItemId);
            b.setPeriod(period);
            b.setQuantity(quantity);
            b.setAmount(amount);
            b.setPaidAmount(BigDecimal.ZERO);
            b.setStatus(1);
            b.setDueDate(dueDate);
            b.setRemark(remark);
            b.setCreateBy(UserContext.getUsername());
            b.setUpdateBy(UserContext.getUsername());
            batch.add(b);
            idx++;
        }
        if (!batch.isEmpty()) {
            billMapper.batchInsert(batch);
            generated = batch.size();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("generated", generated);
        data.put("skipped", skipped);
        return data;
    }

    @Transactional
    public Map<String, Object> pay(Long id, BigDecimal amount, int payMethod, String remark) {
        Bill b = billMapper.selectById(id);
        if (b == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "账单不存在");
        if (b.getStatus() == 4) throw new BusinessException(ResultCode.BILL_VOIDED);
        if (b.getStatus() == 3) throw new BusinessException(ResultCode.BILL_PAID_FULL);
        BigDecimal unpaid = b.getAmount().subtract(b.getPaidAmount());
        if (amount.compareTo(unpaid) > 0) throw new BusinessException(ResultCode.BILL_PAY_EXCEED);

        Payment p = new Payment();
        p.setPaymentNo("PAY" + LocalDateTime.now().format(DAY) + String.format("%05d", paymentMapper.countTodaySeq() + 1));
        p.setBillId(id);
        p.setOwnerId(b.getOwnerId());
        p.setAmount(amount);
        p.setPayMethod(payMethod);
        p.setPayTime(LocalDateTime.now());
        p.setCollectorId(UserContext.getUserId());
        p.setRemark(remark);
        paymentMapper.insert(p);

        BigDecimal newPaid = b.getPaidAmount().add(amount);
        int newStatus = newPaid.compareTo(b.getAmount()) >= 0 ? 3 : 2;
        billMapper.addPaidAmount(id, amount, newStatus, UserContext.getUsername());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("paymentNo", p.getPaymentNo());
        data.put("billStatus", newStatus);
        data.put("paidAmount", newPaid);
        data.put("unpaidAmount", b.getAmount().subtract(newPaid));
        return data;
    }

    @Transactional
    public void voidBill(Long id, String reason) {
        Bill b = billMapper.selectById(id);
        if (b == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (billMapper.countPayments(id) > 0) {
            throw new BusinessException(ResultCode.BILL_HAS_PAYMENT);
        }
        int n = billMapper.voidBill(id, UserContext.getUsername());
        if (n == 0) throw new BusinessException(ResultCode.CONFLICT.getCode(), "当前状态不可作废");
    }

    public void export(Map<String, Object> p, HttpServletResponse response) {
        long total = billMapper.countPage(p);
        List<Map<String, Object>> rows = billMapper.selectPage(p);
        for (Map<String, Object> r : rows) decorate(r);
        try (Workbook wb = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            Sheet sheet = wb.createSheet("账单");
            Row header = sheet.createRow(0);
            String[] cols = {"账单号", "小区", "房号", "业主", "收费项目", "账期", "数量", "应收", "已收", "未收", "状态", "截止日"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int ridx = 1;
            for (Map<String, Object> r : rows) {
                Row row = sheet.createRow(ridx++);
                row.createCell(0).setCellValue(str(r.get("billNo")));
                row.createCell(1).setCellValue(str(r.get("communityName")));
                row.createCell(2).setCellValue(str(r.get("houseNo")));
                row.createCell(3).setCellValue(str(r.get("ownerName")));
                row.createCell(4).setCellValue(str(r.get("feeItemName")));
                row.createCell(5).setCellValue(str(r.get("period")));
                row.createCell(6).setCellValue(str(r.get("quantity")));
                row.createCell(7).setCellValue(str(r.get("amount")));
                row.createCell(8).setCellValue(str(r.get("paidAmount")));
                row.createCell(9).setCellValue(str(r.get("unpaidAmount")));
                row.createCell(10).setCellValue(str(r.get("statusName")));
                row.createCell(11).setCellValue(str(r.get("dueDate")));
            }
            String filename = "bills_" + LocalDate.now().toString().replace("-", "") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            wb.write(out);
            out.flush();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SERVER_ERROR.getCode(), "导出失败：" + e.getMessage());
        }
    }

    private BigDecimal dec(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private Integer intOf(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
