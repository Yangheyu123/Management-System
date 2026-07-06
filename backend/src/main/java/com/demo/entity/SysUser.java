package com.demo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private Integer userType;
    private Long communityId;
    private Integer status;
    private LocalDateTime lastLoginTime;
    /** 入参：角色 id 列表（不入库） */
    private transient List<Long> roleIds;
}
