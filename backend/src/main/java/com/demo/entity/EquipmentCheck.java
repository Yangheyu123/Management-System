package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentCheck {
    private Long id;
    private Long equipmentId;
    private Long checkerId;
    private LocalDateTime checkTime;
    private Integer result;
    private String issueDesc;
    private String images;
    private LocalDateTime createTime;
}
