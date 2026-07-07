package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseEntity {
    private Long parentId;
    private String permName;
    private String permCode;
    private Integer type;
    private String path;
    private String icon;
    private Integer sort;
}
