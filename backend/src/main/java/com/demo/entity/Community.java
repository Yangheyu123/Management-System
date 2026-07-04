package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Community extends BaseEntity {
    private String name;
    private String address;
    private BigDecimal area;
    private BigDecimal greenRate;
    private Integer buildYear;
    private String developer;
    private Integer totalBuildings;
    private Integer totalHouses;
    private String contactName;
    private String contactPhone;
    private Integer status;
}
