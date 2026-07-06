package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Bill extends BaseEntity {
    private String billNo;
    private Long communityId;
    private Long houseId;
    private Long ownerId;
    private Long feeItemId;
    private String period;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status;
    private LocalDate dueDate;
    private String remark;
}
