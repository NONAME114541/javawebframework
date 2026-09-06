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
 * JWT 工具（第15课）。
 * 令牌长这样：xxxxx.yyyyy.zzzzz 三段式——
 *   x=Header(算法)  y=Payload(数据+过期时间)  z=Signature(签名，防篡改)
 * 前两段只是 Base64（谁都能解开看，所以别放密码！），第三段用密钥签名——
 * 改一个字母签名就对不上。可到 jwt.io 现场解码体验。
 */
@Component
public class JwtUtil {

    @Value("${nanpo.jwt.secret}")
    private String secret;

    @Value("${nanpo.jwt.ttl-minutes:120}")
    private long ttlMinutes;

    private SecretKey key() {
        // HS256 要求密钥 ≥ 32 字节
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发令牌：把"谁"（userId/username）和"有效期"写进 Payload 并签名 */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(String.valueOf(userId))          // sub：主题=用户id
                .claim("username", username)               // 自定义声明
                .issuedAt(new Date())                      // iat：签发时间
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(ttlMinutes))))  // exp：过期时间
                .signWith(key())                           // 签名
                .compact();
    }

    /**
     * 解析并校验令牌：签名不对/过期/格式坏都会抛 JwtException 子类。
     * 校验通过返回 Payload（Claims）。
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getTtlMinutes() {
        return ttlMinutes;
    }
}
