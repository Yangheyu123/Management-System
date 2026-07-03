package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class House extends BaseEntity {
    private Long communityId;
    private Long buildingId;
    private String houseNo;
    private String unitNo;
    private Integer floorNo;
    private BigDecimal area;
    private String layout;
    private Integer status;
    private String remark;
}
