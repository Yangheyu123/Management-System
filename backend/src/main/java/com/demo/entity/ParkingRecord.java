package com.demo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ParkingRecord {
    private Long id;
    private Long spaceId;
    private Long ownerId;
    private String plateNo;
    private String action;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long operatorId;
    private LocalDateTime createTime;
}
