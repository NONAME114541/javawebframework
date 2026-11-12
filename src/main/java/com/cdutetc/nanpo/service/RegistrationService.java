package com.cdutetc.nanpo.service;

import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.entity.User;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final UserRoleService roles;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public RegistrationService(UserRepository users, UserRoleService roles) {
        this.users = users;
        this.roles = roles;
    }

    /** 用户与默认角色共享一个事务，任一步失败均回滚。 */
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest request) {
        if (users.findByUsername(request.getUsername()).isPresent())
            throw new BusinessException(ErrorCodeEnum.USERNAME_TAKEN);
        if (request.getPhone() != null && users.findByPhone(request.getPhone()).isPresent())
            throw new BusinessException(ErrorCodeEnum.PHONE_TAKEN);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? request.getUsername() : request.getNickname());
        user.setPhone(request.getPhone());
        user.setCreateTime(LocalDateTime.now());
        User saved = users.saveAndFlush(user);
        roles.assignRequired(saved.getId(), "USER");
        return saved;
    }
}
