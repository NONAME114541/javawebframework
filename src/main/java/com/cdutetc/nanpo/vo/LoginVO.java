package com.cdutetc.nanpo.vo;

import lombok.Data;

/**
 * 登录成功小票（第15课）：发出去的会员卡。
 * 第16课将升级为长短两张卡（accessToken + refreshToken）。
 */
@Data
public class LoginVO {

    private String token;
    private Long expiresIn;   // 有效期（分钟）
    private String nickname;
}
