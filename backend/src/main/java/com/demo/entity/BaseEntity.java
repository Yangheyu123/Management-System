package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private Integer deleted;
}
