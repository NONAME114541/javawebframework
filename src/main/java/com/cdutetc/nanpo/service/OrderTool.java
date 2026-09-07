package com.cdutetc.nanpo.service;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cdutetc.nanpo.common.UserContext;
import com.cdutetc.nanpo.dto.OrderRequest;
import com.cdutetc.nanpo.vo.OrderVO;

/**
 * MCP 下单工具（第20课）：AI 客户端通过 MCP 直接给本店下订单。
 * 注意一个细节：MCP 调用不经过 HTTP 拦截器，没有登录态——
 * 工具里以"AI 来客"系统身份登记下单人，用完立刻清理
 * （第16课 ThreadLocal 的规矩：不清理会串号）。
 */
@Component
public class OrderTool {

    @Autowired
    private OrderService orderService;   // 又是老收银台——MCP 也只是新入口

    @Tool(description = "为顾客下一笔订单：传入菜名和份数，返回订单号与金额")
    public String placeOrder(String dishName, int count) {
        OrderRequest request = new OrderRequest();
        OrderRequest.DishCount dc = new OrderRequest.DishCount();
        dc.setDishName(dishName);
        dc.setCount(count);
        request.setDishList(List.of(dc));
        try {
            UserContext.set(new UserContext.CurrentUser(0L, "ai-guest"));
            OrderVO vo = orderService.createOrder(request);
            return "下单成功：订单号 " + vo.getOrderId() + "，总额 " + vo.getTotalMoney() + " 元";
        } finally {
            UserContext.clear();
        }
    }
}
