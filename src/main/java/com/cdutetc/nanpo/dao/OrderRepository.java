package com.cdutetc.nanpo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cdutetc.nanpo.entity.Order;

/**
 * 订单的数据访问接口。
 * 明细（t_order_item）靠 Order 上的 cascade=ALL 级联保存，
 * 所以暂时不需要单独的 OrderItemRepository。
 *
 * 第12课：查询武器库——
 * 分页 findAll(Pageable) 由 JpaRepository 自带（不用写方法）；
 * 联表查询用 @Query + JPQL：面向对象写 SQL，join 的不是表而是实体关联字段。
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // "谁点过这道菜"：订单表没有菜名字段，跨 t_order/t_order_item 两表查。
    // JPQL 里 join 的是实体的关联属性 o.orderItems，数据库会自动翻译成
    // t_order join t_order_item on order_id = id
    @Query("select distinct o from Order o join o.orderItems i where i.dishName = :dishName")
    List<Order> findByDishName(@Param("dishName") String dishName);
}
