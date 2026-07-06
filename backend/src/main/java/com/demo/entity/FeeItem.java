package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeeItem extends BaseEntity {
    private String name;
    private Integer type;
    private String unit;
    private BigDecimal unitPrice;
    private Integer billingCycle;
    private Integer status;
}
