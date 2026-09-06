package com.cdutetc.nanpo.common;

import lombok.Getter;

/**
 * 全店错误码字典：一眼看出错在哪一层。
 * 200 成功；4xxxx 业务类（顾客的问题）；5xxxx 系统类（店家的问题）
 */
@Getter
public enum ErrorCodeEnum {

    SUCCESS(200, "操作成功"),

    /* 4xxxx 业务类错误 */
    DISH_NOT_FOUND(40401, "该菜品不存在"),
    PATH_NOT_FOUND(40404, "访问路径不存在"),

    /* 5xxxx 系统类错误 */
    SYSTEM_ERROR(50001, "服务器内部错误");

    private final Integer code;
    private final String msg;

    ErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
