package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ParkingSpace extends BaseEntity {
    private Long communityId;
    private String spaceNo;
    private Integer areaType;
    private Integer useType;
    private Long ownerId;
    private String plateNo;
    private BigDecimal monthlyFee;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String remark;
}
