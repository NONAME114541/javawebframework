package com.cdutetc.nanpo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dto.LoginRequest;
import com.cdutetc.nanpo.dto.RefreshRequest;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.service.UserService;
import com.cdutetc.nanpo.vo.LoginVO;

/**
 * 会员窗口：办卡、登录、续卡。
 * 第15课新增：/register /login（单张卡）。
 * 第16课新增：/refresh（长短双卡——长卡换新短卡）。
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

    // 登录：发长短两张卡
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }

    // 续卡：refreshToken 换新的 accessToken（短卡过期了不用重新输密码）
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody RefreshRequest req) {
        return Result.success(userService.refresh(req));
    }
}
