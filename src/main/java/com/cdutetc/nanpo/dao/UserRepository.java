package com.cdutetc.nanpo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdutetc.nanpo.entity.User;

/** 会员的数据访问接口（第15课） */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
