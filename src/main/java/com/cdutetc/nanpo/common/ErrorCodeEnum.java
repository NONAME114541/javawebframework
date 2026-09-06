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
    ORDER_NOT_FOUND(40402, "该订单不存在"),
    FILE_INVALID(40403, "文件为空或格式不支持（仅限图片）"),
    PATH_NOT_FOUND(40404, "访问路径不存在"),
    FILE_NOT_FOUND(40405, "文件不存在"),
    FILE_TOO_LARGE(40406, "上传文件超过大小限制（最大 2MB）"),

    /* 5xxxx 系统类错误 */
    SYSTEM_ERROR(50001, "服务器内部错误");

    private final Integer code;
    private final String msg;

    ErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
