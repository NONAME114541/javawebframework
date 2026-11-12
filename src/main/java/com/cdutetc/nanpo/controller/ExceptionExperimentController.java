package com.cdutetc.nanpo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 实验10：验证未知系统异常由全局处理器统一兜底。 */
@RestController
@RequestMapping("/api/exp10")
public class ExceptionExperimentController {

    @GetMapping("/system-error")
    public void systemError() {
        throw new IllegalStateException("exp10 simulated system failure");
    }
}
