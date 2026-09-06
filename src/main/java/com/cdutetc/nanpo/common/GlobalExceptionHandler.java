package com.cdutetc.nanpo.common;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全店统一的"客诉处理台"：第6课。
 * 无论哪个接口抛出异常，都在这里转成规范的 Result 小票，不再向前端喷异常栈。
 * 三级分流：业务异常(warn) → 路径不存在(404类) → 系统异常(error 兜底)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常：预期之内的小问题（如点了不存在的菜），warn 记录即可
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常 code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // 访问了不存在的路径/静态资源：真实坑——不单独处理的话，
    // 它会掉进下面的 Exception 兜底，被误报成"服务器内部错误"
    // （参考上次课程项目 h2-console 404 被吞成 500 的事故）
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResource(NoResourceFoundException e) {
        log.warn("路径不存在: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.PATH_NOT_FOUND);
    }

    // 第14课兑现第6课的承诺：上传超限异常接入处理台——
    // multipart 解析阶段就拒绝，根本到不了 Controller
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过大小限制: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.FILE_TOO_LARGE);
    }

    // 系统异常兜底：真事故——error 级别记录完整堆栈，对顾客只说"内部错误"
    @ExceptionHandler(Exception.class)
    public Result<?> handleAll(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ErrorCodeEnum.SYSTEM_ERROR);
    }
}
