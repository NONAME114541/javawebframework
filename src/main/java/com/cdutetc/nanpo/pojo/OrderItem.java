package com.cdutetc.nanpo.pojo;

import lombok.Data;

/**
 * 订单明细：一道菜买了几份、单价多少、小计多少。
 */
@Data
public class OrderItem {

    private String dishName;
    private Integer count;
    private Integer unitPrice;
    private Integer subTotal;
}
