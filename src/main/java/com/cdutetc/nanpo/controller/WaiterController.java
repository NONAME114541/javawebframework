package com.cdutetc.nanpo.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.entity.Menu;
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
 * 第6课：客诉统一——业务问题抛 BusinessException，由 GlobalExceptionHandler 全局兜住。
 * 第8课：菜谱上账——静态 MENU Map 彻底删除，验菜取价全部查数据库 t_menu。
 */
@RestController
public class WaiterController {

    // 第8课：从第2课用到现在的静态 MENU Map 已删除——菜单的唯一权威来源是 t_menu 表，
    // 改价只需 update 数据库，再也不用改代码重新发版

    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private SideDishService sideDishService;
    @Autowired
    private CookService cookService;

    // 查菜单：第8课整改——数据来源换成 t_menu 表。
    // 对外仍保持 Map{菜名:价格} 的形状，前端零改动：换数据源，不动对外契约
    @GetMapping("/menu")
    public Result<Map<String, BigDecimal>> getMenu() {
        Map<String, BigDecimal> menu = new LinkedHashMap<>();
        menuRepository.findAll().forEach(m -> menu.put(m.getDishName(), m.getPrice()));
        return Result.success(menu);
    }

    // 点单：@RequestBody 把前端提交的 JSON 自动装配成 OrderRequest 对象
    @PostMapping("/order")
    public Result<OrderResponse> order(@RequestBody OrderRequest orderRequest) {
        OrderResponse resp = new OrderResponse();
        List<OrderItem> itemList = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.DishCount dc : orderRequest.getDishList()) {
            String dishName = dc.getDishName();
            Integer count = dc.getCount();

            // 第8课整改：验菜+取价一步完成——查 t_menu，
            // 查不到就抛业务异常（第6课的处理台会转成规范小票）
            Menu menu = menuRepository.findByDishName(dishName)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND));

            BigDecimal subTotal = menu.getPrice().multiply(BigDecimal.valueOf(count));
            total = total.add(subTotal);

            // 第3课：厨房分工——墩子先配菜，厨师再炒菜
            sideDishService.prepareSideMaterial(dishName, count);
            cookService.cookDish(dishName, count);

            OrderItem item = new OrderItem();
            item.setDishName(dishName);
            item.setCount(count);
            item.setUnitPrice(menu.getPrice());
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
