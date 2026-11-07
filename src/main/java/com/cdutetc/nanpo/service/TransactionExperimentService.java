package com.cdutetc.nanpo.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.entity.User;

/** 故障注入只在实验 profile 注册，跨 Service 调用确保经过事务代理。 */
@Profile("exp08")
@Service
public class TransactionExperimentService {
    private final RegistrationService registration;
    private final UserRoleService roles;

    public TransactionExperimentService(RegistrationService registration, UserRoleService roles) {
        this.registration = registration;
        this.roles = roles;
    }

    @Transactional
    public void duplicateRole(RegisterRequest request) {
        User user = registration.register(request); // 已写入用户与第一个 USER 角色。
        roles.assignRequired(user.getId(), "USER"); // 真实唯一约束冲突，整个事务回滚。
    }

    @Transactional
    public void outerRollback(RegisterRequest request, Long existingUserId, boolean requiresNew) {
        registration.register(request); // 外层事务内的新用户与默认角色。
        if (requiresNew) roles.assignRequiresNew(existingUserId, "NEW_DEMO");
        else roles.assignRequired(existingUserId, "REQUIRED_DEMO");
        throw new IllegalStateException("exp08: force outer rollback");
    }
}
