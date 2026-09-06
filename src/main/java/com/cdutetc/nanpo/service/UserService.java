package com.cdutetc.nanpo.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.common.JwtUtil;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.dto.LoginRequest;
import com.cdutetc.nanpo.dto.RefreshRequest;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.entity.User;
import com.cdutetc.nanpo.vo.LoginVO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/**
 * 会员服务：办卡（注册）、验证身份（登录）、续卡（refresh）。
 * 第15课：单张 120 分钟卡。
 * 第16课：长短双卡——登录同时发 access(15分钟)+refresh(7天)，
 * 短卡过期用长卡续，避免用户天天重新输密码。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 只借 BCrypt 工具类，不引 Spring Security 全家桶（口头对比生产做法）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /** 办卡：用户名查重 + 密码加密入库 */
    public void register(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCodeEnum.USERNAME_TAKEN);
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    /** 登录：验证通过发长短两张卡 */
    public LoginVO login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.LOGIN_FAILED));
        // 用户不存在和密码错误统一报错——不帮攻击者缩小爆破范围
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCodeEnum.LOGIN_FAILED);
        }
        return buildLoginVO(user);
    }

    /** 续卡：长卡换新短卡（长卡本身沿用；生产建议每次轮换 refreshToken——口头展开） */
    public LoginVO refresh(RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(req.getRefreshToken());
        } catch (JwtException e) {
            throw new BusinessException(ErrorCodeEnum.TOKEN_INVALID);
        }
        // 短卡不能来续卡——各卡各用（用 accessToken 调这里会被拒）
        if (!JwtUtil.TYPE_REFRESH.equals(claims.get("type", String.class))) {
            throw new BusinessException(ErrorCodeEnum.TOKEN_INVALID);
        }
        User user = userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.TOKEN_INVALID));
        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setAccessToken(jwtUtil.generateAccessToken(user.getId(), user.getUsername()));
        vo.setRefreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getUsername()));
        vo.setAccessExpiresIn(jwtUtil.getAccessTtlMinutes());
        vo.setNickname(user.getNickname());
        return vo;
    }
}
