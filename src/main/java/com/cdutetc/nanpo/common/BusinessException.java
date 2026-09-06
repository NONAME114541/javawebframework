package com.cdutetc.nanpo.common;

import lombok.Getter;

/**
 * 业务异常：顾客的问题（点的菜不存在、库存不够…），对应 4xxxx 错误码。
 * 第6课引入：和"系统异常"区分开——
 * 业务异常是正常经营中的预期情况，记 warn 级别即可；
 * 系统异常才是真事故，要记 error 并排查。
 */
public class BusinessException extends RuntimeException {

    @Getter
    private final Integer code;

    public BusinessException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMsg());
        this.code = errorCodeEnum.getCode();
    }
}
