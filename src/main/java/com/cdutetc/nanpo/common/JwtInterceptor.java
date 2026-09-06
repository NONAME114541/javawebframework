package com.cdutetc.nanpo.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 查票员（第16课）：统一的令牌校验拦截器。
 * 请求头格式：Authorization: Bearer <accessToken>
 * 校验通过 → 当前用户放进 UserContext，放行；
 * 任何问题 → 抛业务异常（第6课的处理台会转成规范 JSON）。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCodeEnum.TOKEN_MISSING);
        }
        String token = auth.substring(7);   // 去掉 "Bearer " 前缀
        try {
            Claims claims = jwtUtil.parseClaims(token);
            // 长短卡规矩：refreshToken 不能当 accessToken 用（续卡专用，见 /refresh）
            if (!JwtUtil.TYPE_ACCESS.equals(claims.get("type", String.class))) {
                throw new BusinessException(ErrorCodeEnum.TOKEN_INVALID);
            }
            UserContext.set(new UserContext.CurrentUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class)));
            return true;
        } catch (JwtException e) {
            // 过期(ExpiredJwtException)/签名不对/格式坏，都归为"令牌无效"
            throw new BusinessException(ErrorCodeEnum.TOKEN_INVALID);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程池复用线程，必须清理——否则下一个请求可能"冒充"上一个登录人
        UserContext.clear();
    }
}
