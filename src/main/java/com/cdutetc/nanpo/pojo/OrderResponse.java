package com.cdutetc.nanpo.pojo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 点单响应：返回给前端的"小票"。
 * 第8课：totalMoney 从 Integer 升级为 BigDecimal（与菜单价格口径一致）。
 */
@Data
public class OrderResponse {

    private List<OrderItem> itemList;
    private BigDecimal totalMoney;
    private String msg;
}
