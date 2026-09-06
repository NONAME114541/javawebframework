package com.cdutetc.nanpo.vo;

import java.math.BigDecimal;

import com.cdutetc.nanpo.entity.OrderItem;

import lombok.Data;

/**
 * 小票明细行 VO（出参：后端 → 前端）。
 * 第10课新增：字段按前端需要从订单明细实体裁剪而来——
 * 实体上的 id、order 外键等账本细节，前端不需要就不给看。
 */
@Data
public class OrderItemVO {

    private String dishName;
    private Integer count;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;

    // 静态工厂转换：实体 → VO 集中一处，改字段只动这一个文件
    public static OrderItemVO from(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setDishName(item.getDishName());
        vo.setCount(item.getCount());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setSubTotal(item.getSubTotal());
        return vo;
    }
}
