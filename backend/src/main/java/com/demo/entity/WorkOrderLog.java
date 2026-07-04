package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkOrderLog {
    private Long id;
    private Long orderId;
    private Long operatorId;
    private String operatorName;
    private String action;
    private Integer fromStatus;
    private Integer toStatus;
    private String remark;
    private LocalDateTime createTime;
}
