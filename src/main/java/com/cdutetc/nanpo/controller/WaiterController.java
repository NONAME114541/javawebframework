package com.cdutetc.nanpo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.pojo.OrderItem;
import com.cdutetc.nanpo.pojo.OrderRequest;
import com.cdutetc.nanpo.pojo.OrderResponse;
import com.cdutetc.nanpo.service.CookService;
import com.cdutetc.nanpo.service.SideDishService;

/**
 * 服务员。
 * 第2课：一个人包揽所有活——背菜谱、验菜算账、还要顺手"炒菜"。
 * 第3课：招入墩子和厨师，形成调用链：服务员 → 墩子 → 厨师（但员工是自己 new 的）。
 * 第4课：人事权上交——员工由 IoC 容器统一委派（@Autowired 依赖注入）。
 * 第5课：小票格式统一——所有返回都包上 Result{code, msg, data}。
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

    // 第4课：由第3课的"手动 new"改为容器注入——控制权反转(IoC)
    @Autowired
    private SideDishService sideDishService;
    @Autowired
    private CookService cookService;

    // 查菜单：第5课整改——从第2课的"裸 Map"换成统一小票 Result
    @GetMapping("/menu")
    public Result<Map<String, Integer>> getMenu() {
        return Result.success(MENU);
    }

    // 点单：@RequestBody 把前端提交的 JSON 自动装配成 OrderRequest 对象
    @PostMapping("/order")
    public Result<OrderResponse> order(@RequestBody OrderRequest orderRequest) {
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

            // 第3课：厨房分工——墩子先配菜，厨师再炒菜
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
        // 第5课整改：小票也包进统一返回
        return Result.success(resp);
    }
}
