package com.demo.entity;

import lombok.Data;

@Data
public class OwnerHouse {
    private Long id;
    private Long ownerId;
    private Long houseId;
    private String relation;
    private Integer isPrimary;
}
