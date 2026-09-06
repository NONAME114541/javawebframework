package com.cdutetc.nanpo.common;

import lombok.Data;

/**
 * 统一返回"小票"：第5课起，全店所有接口的返回都是这个格式。
 * {"code": 200, "msg": "操作成功", "data": {...}}
 *
 * @param <T> data 的类型——泛型让同一张小票能装任何数据（菜单、订单、字符串…）
 */
@Data
public class Result<T> {

    private Integer code;   // 200 成功；4xxxx 业务失败；5xxxx 系统错误
    private String msg;     // 给人看的信息
    private T data;         // 给程序用的数据

    // ---- 静态工厂：调用方一行搞定，不用每次 new + setXxx 三连 ----

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(ErrorCodeEnum.SUCCESS.getCode());
        r.setMsg(ErrorCodeEnum.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static <T> Result<T> fail(ErrorCodeEnum errorCodeEnum) {
        return fail(errorCodeEnum.getCode(), errorCodeEnum.getMsg());
    }
}
