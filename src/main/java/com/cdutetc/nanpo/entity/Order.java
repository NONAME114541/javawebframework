package com.cdutetc.nanpo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 订单实体（订单主表）。
 * 表名用 t_order：order 是 SQL 保留字（ORDER BY 的 ORDER），直接用会语法报错。
 *
 * 一对多的"一方"：mappedBy 表示关系由对方（OrderItem 的 order 字段）维护，
 * 不会额外生成中间表；cascade=ALL 让保存订单时明细一并入库。
 */
@Entity
@Table(name = "t_order")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal totalAmount;

    private LocalDateTime createTime;

    // 双向关联里 lombok 的 toString/equals 会沿着 order→items→order 互相调用，
    // 不排除会无限递归——两个 @xxxExclude 是规矩写法
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems = new ArrayList<>();

    // 双向关联辅助方法：两边同时赋值，外键 order_id 才能正确回填
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}
