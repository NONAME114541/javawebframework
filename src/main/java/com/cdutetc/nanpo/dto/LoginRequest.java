package com.cdutetc.nanpo.dto;

import lombok.Data;

/** 登录请求（第15课） */
@Data
public class LoginRequest {

    private String username;
    private String password;
}
