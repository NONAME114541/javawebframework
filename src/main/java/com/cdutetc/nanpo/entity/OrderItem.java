package com.cdutetc.nanpo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 订单明细实体（订单明细表）：一单多个菜，每个菜一行。
 *
 * 一对多的"多方"：外键列 order_id 定义在这一侧（@JoinColumn）；
 * LAZY 懒加载——查明细时才顺带取订单，不主动查就不发 SQL。
 */
@Entity
@Table(name = "t_order_item")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    private String dishName;

    private Integer count;

    /** 价格快照：下单那一刻的菜单价。之后菜单调价，历史订单金额不变。 */
    private BigDecimal unitPrice;

    private BigDecimal subTotal;

    // 不用 @AllArgsConstructor（它会带上 id 和 order），手写业务用得上的四参构造器
    public OrderItem(String dishName, Integer count, BigDecimal unitPrice, BigDecimal subTotal) {
        this.dishName = dishName;
        this.count = count;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }
}
