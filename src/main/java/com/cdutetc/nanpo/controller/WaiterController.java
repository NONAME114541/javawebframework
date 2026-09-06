package com.cdutetc.nanpo.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.dto.OrderRequest;
import com.cdutetc.nanpo.service.OrderService;
import com.cdutetc.nanpo.vo.OrderPageVO;
import com.cdutetc.nanpo.vo.OrderVO;

/**
 * 服务员。
 * 第2课：一个人包揽所有活——背菜谱、验菜算账、还要顺手"炒菜"。
 * 第3课：招入墩子和厨师，形成调用链：服务员 → 墩子 → 厨师（但员工是自己 new 的）。
 * 第4课：人事权上交——员工由 IoC 容器统一委派（@Autowired 依赖注入）。
 * 第5课：小票格式统一——所有返回都包上 Result{code, msg, data}。
 * 第6课：客诉统一——业务问题抛 BusinessException，由 GlobalExceptionHandler 全局兜住。
 * 第8课：菜谱上账——验菜取价全部查数据库 t_menu。
 * 第9课：服务员瘦身——下单业务搬进 OrderService（收银台），本类只剩 HTTP 进出。
 * 第10课：入参 dto.OrderRequest、出参 vo.OrderVO——Controller 的世界再无实体。
 */
@RestController
public class WaiterController {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderService orderService;

    // 查菜单：数据来源 t_menu 表，对外保持 Map{菜名:价格}，前端零改动
    @GetMapping("/menu")
    public Result<Map<String, BigDecimal>> getMenu() {
        Map<String, BigDecimal> menu = new LinkedHashMap<>();
        menuRepository.findAll().forEach(m -> menu.put(m.getDishName(), m.getPrice()));
        return Result.success(menu);
    }

    // 点单：接单（dto 入参）→ 调收银台 → 回话（vo 出参）
    @PostMapping("/order")
    public Result<OrderVO> order(@RequestBody OrderRequest orderRequest) {
        return Result.success(orderService.createOrder(orderRequest));
    }

    // ---- 第12课：翻账本 ----

    // 分页查订单：页号从 0 开始（@RequestParam 可带默认值），按下单时间倒序
    @GetMapping("/order/list")
    public Result<OrderPageVO> listOrders(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size) {
        return Result.success(orderService.getOrderPage(page, size));
    }

    // 订单详情：路径变量 /order/1（与 /order/list、/order/search 字面量精确匹配优先）
    @GetMapping("/order/{id}")
    public Result<OrderVO> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrder(id));
    }

    // 按菜名查订单："谁点过回锅肉"（JPQL 联表）
    @GetMapping("/order/search")
    public Result<List<OrderVO>> searchByDish(@RequestParam String dish) {
        return Result.success(orderService.searchByDish(dish));
    }
}
