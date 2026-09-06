package com.cdutetc.nanpo.dto;

import lombok.Data;

/** 续卡请求（第16课）：带上长的 refreshToken 换一张新的 accessToken */
@Data
public class RefreshRequest {

    private String refreshToken;
}
