package com.cdutetc.nanpo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdutetc.nanpo.entity.Order;

/**
 * 订单的数据访问接口。
 * 明细（t_order_item）靠 Order 上的 cascade=ALL 级联保存，
 * 所以暂时不需要单独的 OrderItemRepository。
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
