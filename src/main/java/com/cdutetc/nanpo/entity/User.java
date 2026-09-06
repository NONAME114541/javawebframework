package com.cdutetc.nanpo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员实体（第15课）：谁在我店里有卡。
 * 注意：password 字段绝不放进任何 VO——第10课"实体不能外泄"的伏笔在这里兑现，
 * 将来谁把 User 实体直接序列化出去，密码哈希就裸奔了。
 */
@Entity
@Table(name = "t_user")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    /** BCrypt 哈希（$2a$10$... 共60位）——绝不存明文；同一密码每次加密结果都不同（自带随机盐） */
    @Column(nullable = false)
    private String password;

    private String nickname;

    private LocalDateTime createTime;
}
