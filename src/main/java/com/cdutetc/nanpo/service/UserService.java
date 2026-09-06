package com.cdutetc.nanpo.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.common.JwtUtil;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.dto.LoginRequest;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.entity.User;
import com.cdutetc.nanpo.vo.LoginVO;

/**
 * 会员服务（第15课）：办卡（注册）与验证身份（登录发 JWT）。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 只借 BCrypt 工具类，不引 Spring Security 全家桶（第16课口头对比生产做法）
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
        // BCrypt：自带随机盐——同一个密码每次加密结果都不同，
        // 数据库被拖走也倒推不出原密码
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    /** 登录：验证通过就发 JWT 会员卡 */
    public LoginVO login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.LOGIN_FAILED));
        // 注意：用户不存在和密码错误统一报"用户名或密码错误"——
        // 不帮攻击者缩小爆破范围
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCodeEnum.LOGIN_FAILED);
        }

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        vo.setExpiresIn(jwtUtil.getTtlMinutes());
        vo.setNickname(user.getNickname());
        return vo;
    }
}
