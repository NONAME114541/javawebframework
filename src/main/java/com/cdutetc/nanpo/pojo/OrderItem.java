package com.cdutetc.nanpo.pojo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 订单明细：一道菜买了几份、单价多少、小计多少。
 * 第8课：金额字段从 Integer 升级为 BigDecimal——钱不用整数/浮点存是行业规矩。
 */
@Data
public class OrderItem {

    private String dishName;
    private Integer count;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
