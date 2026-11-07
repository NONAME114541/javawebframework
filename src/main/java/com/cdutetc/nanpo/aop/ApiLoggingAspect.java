package com.cdutetc.nanpo.aop;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dto.LoginRequest;
import com.cdutetc.nanpo.dto.RefreshRequest;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.vo.LoginVO;

/** 接口横切日志：正常返回与异常均计时；敏感字段不写入日志。 */
@Aspect
@Component
public class ApiLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAspect.class);

    @Around("execution(public * com.cdutetc.nanpo.controller..*(..))")
    public Object record(ProceedingJoinPoint point) throws Throwable {
        long start = System.nanoTime();
        String method = point.getSignature().toShortString();
        List<Object> args = new ArrayList<>();
        for (Object arg : point.getArgs()) args.add(safe(arg));
        log.info("[exp08-aop] start={} method={} args={}", Instant.now(), method, args);
        try {
            Object result = point.proceed();
            log.info("[exp08-aop] method={} result={} elapsedMs={}",
                    method, safe(result), (System.nanoTime() - start) / 1_000_000.0);
            return result;
        } catch (Throwable error) {
            log.warn("[exp08-aop] method={} exception={} elapsedMs={}",
                    method, error.getClass().getSimpleName(),
                    (System.nanoTime() - start) / 1_000_000.0);
            throw error; // 必须向调用方传播，不能吞掉事务失败。
        }
    }

    private Object safe(Object value) {
        if (value == null) return "null";
        if (value instanceof RegisterRequest r)
            return Map.of("username", safe(r.getUsername()), "nickname", safe(r.getNickname()),
                    "password", "[REDACTED]");
        if (value instanceof LoginRequest r)
            return Map.of("username", safe(r.getUsername()), "password", "[REDACTED]");
        if (value instanceof RefreshRequest) return Map.of("refreshToken", "[REDACTED]");
        if (value instanceof LoginVO r)
            return Map.of("nickname", safe(r.getNickname()), "accessToken", "[REDACTED]",
                    "refreshToken", "[REDACTED]");
        if (value instanceof Result<?> r)
            return Map.of("code", r.getCode(), "msg", safe(r.getMsg()), "data", safe(r.getData()));
        if (value instanceof Map<?, ?> m) {
            Map<String, Object> copy = new LinkedHashMap<>();
            m.forEach((k, v) -> {
                String key = String.valueOf(k);
                copy.put(key, key.toLowerCase().matches(".*(password|token|secret).*" )
                        ? "[REDACTED]" : safe(v));
            });
            return copy;
        }
        if (value instanceof Iterable<?> items) {
            List<Object> copy = new ArrayList<>();
            for (Object item : items) { if (copy.size() == 20) break; copy.add(safe(item)); }
            return copy;
        }
        if (value instanceof Number || value instanceof Boolean) return value;
        if (value instanceof String s) {
            String oneLine = s.replace('\n', ' ').replace('\r', ' ');
            return oneLine.length() > 160 ? oneLine.substring(0, 160) + "..." : oneLine;
        }
        return value.getClass().getSimpleName(); // 未知对象不反射展开，避免意外泄露。
    }
}
