package com.demo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private String paymentNo;
    private Long billId;
    private Long ownerId;
    private BigDecimal amount;
    private Integer payMethod;
    private LocalDateTime payTime;
    private Long collectorId;
    private String remark;
    private LocalDateTime createTime;
}
