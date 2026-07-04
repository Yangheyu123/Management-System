package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorkOrder extends BaseEntity {
    private String orderNo;
    private Long communityId;
    private Long houseId;
    private Long ownerId;
    private String title;
    private Integer type;
    private Integer priority;
    private String description;
    private String images;
    private Integer status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime handleTime;
    private LocalDateTime finishTime;
    private Integer rating;
    private String ratingComment;
}
