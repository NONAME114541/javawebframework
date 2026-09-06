package com.cdutetc.nanpo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;

/**
 * 第1课验证接口：店开了没有？
 * 第5课：小票格式全店统一，这个接口也顺手包上 Result。
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("南坡小炒，本店开业！");
    }
}
