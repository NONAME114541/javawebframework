package com.cdutetc.nanpo.common;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 工具。
 * 第15课：单张卡（120 分钟）。
 * 第16课升级为长短两张卡：
 *   accessToken（15分钟，天天用）+ refreshToken（7天，只用来续卡）——
 *   短卡丢了损失小，长卡尽量少出门，被偷的风险就小；
 *   两张卡靠 Payload 里的 type 声明区分，各卡各用、不能混用。
 */
@Component
public class JwtUtil {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    @Value("${nanpo.jwt.secret}")
    private String secret;

    @Value("${nanpo.jwt.access-ttl-minutes:15}")
    private long accessTtlMinutes;

    @Value("${nanpo.jwt.refresh-ttl-days:7}")
    private long refreshTtlDays;

    private SecretKey key() {
        // HS256 要求密钥 ≥ 32 字节
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username) {
        return generate(userId, username, Duration.ofMinutes(accessTtlMinutes), TYPE_ACCESS);
    }

    public String generateRefreshToken(Long userId, String username) {
        return generate(userId, username, Duration.ofDays(refreshTtlDays), TYPE_REFRESH);
    }

    private String generate(Long userId, String username, Duration ttl, String type) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", type)                              // access / refresh
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(ttl)))
                .signWith(key())
                .compact();
    }

    /** 解析并校验：签名不对/过期/格式坏抛 JwtException 子类，通过则返回 Payload */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTtlMinutes() {
        return accessTtlMinutes;
    }
}
