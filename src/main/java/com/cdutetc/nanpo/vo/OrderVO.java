package com.cdutetc.nanpo.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.cdutetc.nanpo.entity.Order;
import com.cdutetc.nanpo.entity.OrderItem;

import lombok.Data;

/**
 * 订单小票 VO（出参：后端 → 前端）。
 * 第10课新增：账本（entity.Order）不能直接发给前端——
 * 双向关联会被 JSON 无限递归、懒加载随时炸、实体字段（如以后的密码）全部裸奔；
 * VO 按前端需要裁剪字段，实体永不出 Service 之外。
 */
@Data
public class OrderVO {

    private Long orderId;
    private List<OrderItemVO> itemList;
    private BigDecimal totalMoney;
    private String msg;

    // 静态工厂转换：账本 → 小票。第9课里 pojo 与实体同名的别扭（全限定名）
    // 在这一步彻底消失——pojo 退役，vo 与 entity 各归各位、井水不犯河水
    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getId());
        vo.setTotalMoney(order.getTotalAmount());
        List<OrderItemVO> items = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            items.add(OrderItemVO.from(item));
        }
        vo.setItemList(items);
        return vo;
    }
}
