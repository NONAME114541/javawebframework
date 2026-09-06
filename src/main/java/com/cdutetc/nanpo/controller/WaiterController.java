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
import com.cdutetc.nanpo.service.CookService;
import com.cdutetc.nanpo.service.SideDishService;

/**
 * 服务员。
 * 第2课：一个人包揽所有活——背菜谱、验菜算账、还要顺手"炒菜"。
 * 第3课：店里招人了——配菜交给墩子(SideDishService)、炒菜交给厨师(CookService)，
 *        服务员只负责接单、验菜、算账，形成调用链：服务员 → 墩子 → 厨师。
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

    // 第3课：服务员自己"招人"——手动 new。
    // 想想有什么问题：谁都能 new 一份、无法统一管理、多个服务员无法共享同一批员工……
    // 这些问题正是第4课 IoC 容器要解决的
    private final SideDishService sideDishService = new SideDishService();
    private final CookService cookService = new CookService();

    // 查菜单：故意返回裸 Map——第5课"统一返回"时回来整改
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
            // 浏览器会看到丑陋的 500 错误页——第6课"统一异常处理"回来整改
            if (!MENU.containsKey(dishName)) {
                throw new RuntimeException("菜品不存在：" + dishName);
            }
            int unitPrice = MENU.get(dishName);
            int subTotal = unitPrice * count;
            total += subTotal;

            // 第3课：厨房分工——墩子先配菜，厨师再炒菜。
            // （第2课的"[服务员] 炒好了"打印已删除：炒菜不再是服务员的活）
            sideDishService.prepareSideMaterial(dishName, count);
            cookService.cookDish(dishName, count);

            OrderItem item = new OrderItem();
            item.setDishName(dishName);
            item.setCount(count);
            item.setUnitPrice(unitPrice);
            item.setSubTotal(subTotal);
            itemList.add(item);
        }

        resp.setItemList(itemList);
        resp.setTotalMoney(total);
        resp.setMsg("下单成功，请到取餐口稍等～");
        return resp;
    }
}
