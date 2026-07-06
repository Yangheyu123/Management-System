package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Building extends BaseEntity {
    private Long communityId;
    private String name;
    private String buildingNo;
    private Integer floors;
    private Integer units;
    private Integer elevators;
    private String structureType;
    private String remark;
}
