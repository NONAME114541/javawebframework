package com.cdutetc.nanpo.vo;

/** 查询视图仅包含公开资料，禁止将密码或密码哈希返回给客户端。 */
public record UserVO(Long id, String username, String nickname) {
}
