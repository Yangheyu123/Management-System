package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Owner extends BaseEntity {
    private String name;
    private String phone;
    private String idCard;
    private Integer gender;
    private String plateNo;
    private LocalDate moveInDate;
    private Integer status;
    private String remark;
}
