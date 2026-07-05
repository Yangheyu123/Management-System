package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Equipment extends BaseEntity {
    private Long communityId;
    private Integer category;
    private String name;
    private String code;
    private String location;
    private String model;
    private String manufacturer;
    private LocalDate installDate;
    private LocalDate warrantyDate;
    private Integer onlineStatus;
    private Integer status;
    private LocalDate lastCheckDate;
    private LocalDate nextCheckDate;
    private String remark;
}
