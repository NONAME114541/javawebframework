package com.cdutetc.nanpo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dto.LoginRequest;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.service.UserService;
import com.cdutetc.nanpo.vo.LoginVO;

/**
 * 会员窗口（第15课）：办卡与登录。
 * 第三个 Controller——店里新开了会员窗口。
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // 办会员卡
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest req) {
        userService.register(req);
        return Result.success();
    }

    // 登录：验证通过发 JWT 令牌
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }
}
