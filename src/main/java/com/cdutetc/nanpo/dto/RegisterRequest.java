package com.cdutetc.nanpo.dto;

import lombok.Data;

/** 注册请求（第15课） */
@Data
public class RegisterRequest {

    private String username;
    private String password;
    private String nickname;   // 可选，不填默认用用户名
}
