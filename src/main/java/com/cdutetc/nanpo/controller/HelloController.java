package com.cdutetc.nanpo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第1课验证接口：店开了没有？
 * 浏览器或 REST Client 访问 http://localhost:8080/hello
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "南坡小炒，本店开业！";
    }
}
