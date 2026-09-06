package com.cdutetc.nanpo.pojo;

import java.util.List;

import lombok.Data;

/**
 * 点单响应：返回给前端的"小票"。
 */
@Data
public class OrderResponse {

    private List<OrderItem> itemList;
    private Integer totalMoney;
    private String msg;
}
