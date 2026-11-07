package com.cdutetc.nanpo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cdutetc.nanpo.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    long countByUserUsername(String username);
    long countByUserUsernameAndRoleName(String username, String roleName);
}
