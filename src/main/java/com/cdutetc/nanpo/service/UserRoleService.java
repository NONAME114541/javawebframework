package com.cdutetc.nanpo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.dao.UserRoleRepository;
import com.cdutetc.nanpo.entity.UserRole;

@Service
public class UserRoleService {
    private final UserRepository users;
    private final UserRoleRepository roles;

    public UserRoleService(UserRepository users, UserRoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void assignRequired(Long userId, String role) {
        roles.saveAndFlush(new UserRole(users.getReferenceById(userId), role));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assignRequiresNew(Long userId, String role) {
        roles.saveAndFlush(new UserRole(users.getReferenceById(userId), role));
    }
}
