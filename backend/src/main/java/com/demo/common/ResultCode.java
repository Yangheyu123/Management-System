package com.demo.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效，请重新登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    SERVER_ERROR(500, "系统繁忙，请稍后再试"),

    USERNAME_OR_PASSWORD_ERROR(2001, "用户名或密码错误"),
    ACCOUNT_DISABLED(2002, "账号已被禁用，请联系管理员"),
    OLD_PASSWORD_ERROR(2003, "旧密码错误"),
    USERNAME_EXISTS(2004, "用户名已存在"),

    WORKORDER_STATUS_ILLEGAL(2101, "当前工单状态不允许该操作"),
    WORKORDER_ALREADY_ACCEPTED(2102, "该工单已被他人接单"),

    BILL_PAID_FULL(2201, "账单已缴清"),
    BILL_PAY_EXCEED(2202, "缴费金额超过应收金额"),
    BILL_VOIDED(2203, "账单已作废"),
    BILL_HAS_PAYMENT(2204, "账单已产生缴费，不可作废，请走冲正流程"),

    PARKING_OCCUPIED(2301, "车位已被占用"),

    EQUIPMENT_CODE_EXISTS(2401, "设备编号已存在"),

    UNKNOWN(9999, "未知系统错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
