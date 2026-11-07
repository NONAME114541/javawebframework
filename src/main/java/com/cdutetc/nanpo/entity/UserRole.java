package com.cdutetc.nanpo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户角色分配表：同一用户同一角色不可重复，供真实数据库约束验证。 */
@Entity
@Table(name = "t_user_role", uniqueConstraints =
        @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_name"}))
@Data
@NoArgsConstructor
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    public UserRole(User user, String roleName) {
        this.user = user;
        this.roleName = roleName;
    }
}
