package com.cdutetc.nanpo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.pojo.OrderItem;
import com.cdutetc.nanpo.pojo.OrderRequest;
import com.cdutetc.nanpo.pojo.OrderResponse;

/**
 * 服务员。
 * 第2课的店里只有服务员一个人：他既背菜谱、又验菜算账、还要顺手"炒菜"——
 * 所有活都挤在这一个类里，正是第3课"招人分工（三层架构）"要解决的问题。
 */
@RestController
public class WaiterController {

    // 菜谱暂且写死在代码里（第8课将搬进数据库）
    private static final Map<String, Integer> MENU = new HashMap<>();
    static {
        MENU.put("回锅肉", 18);
        MENU.put("鱼香肉丝", 16);
        MENU.put("麻婆豆腐", 12);
        MENU.put("番茄炒蛋", 10);
        MENU.put("蛋炒饭", 8);
        MENU.put("米饭", 1);
    }

    // 查菜单：故意返回裸 Map，格式不规范——第5课"统一返回"时回来整改
    @GetMapping("/menu")
    public Map<String, Integer> getMenu() {
        return MENU;
    }

    // 点单：@RequestBody 把前端提交的 JSON 自动装配成 OrderRequest 对象
    @PostMapping("/order")
    public OrderResponse order(@RequestBody OrderRequest orderRequest) {
        OrderResponse resp = new OrderResponse();
        List<OrderItem> itemList = new ArrayList<>();
        int total = 0;

        for (OrderRequest.DishCount dc : orderRequest.getDishList()) {
            String dishName = dc.getDishName();
            Integer count = dc.getCount();

            // 菜单里没有这道菜：直接抛出运行时异常。
            // 浏览器/REST Client 会看到丑陋的 500 错误页——第6课"统一异常处理"回来整改
            if (!MENU.containsKey(dishName)) {
                throw new RuntimeException("菜品不存在：" + dishName);
            }
            int unitPrice = MENU.get(dishName);
            int subTotal = unitPrice * count;
            total += subTotal;

            OrderItem item = new OrderItem();
            item.setDishName(dishName);
            item.setCount(count);
            item.setUnitPrice(unitPrice);
            item.setSubTotal(subTotal);
            itemList.add(item);

            // 服务员顺手把菜也"炒"了（第3课把这份活交给厨师和墩子）
            System.out.printf("[服务员] 炒好了 %s x %d%n", dishName, count);
        }

        resp.setItemList(itemList);
        resp.setTotalMoney(total);
        resp.setMsg("下单成功，请到取餐口稍等～");
        return resp;
    }
}
