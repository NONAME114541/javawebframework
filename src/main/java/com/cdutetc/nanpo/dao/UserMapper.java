package com.cdutetc.nanpo.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.cdutetc.nanpo.entity.User;

/** 实验07：数据访问 Bean，复用现有 JPA Repository，不另建数据库。 */
@Repository
public class UserMapper {

    private final UserRepository userRepository;

    public UserMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
