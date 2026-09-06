package com.cdutetc.nanpo.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.pojo.OrderRequest;
import com.cdutetc.nanpo.pojo.OrderResponse;
import com.cdutetc.nanpo.service.OrderService;

/**
 * 服务员。
 * 第2课：一个人包揽所有活——背菜谱、验菜算账、还要顺手"炒菜"。
 * 第3课：招入墩子和厨师，形成调用链：服务员 → 墩子 → 厨师（但员工是自己 new 的）。
 * 第4课：人事权上交——员工由 IoC 容器统一委派（@Autowired 依赖注入）。
 * 第5课：小票格式统一——所有返回都包上 Result{code, msg, data}。
 * 第6课：客诉统一——业务问题抛 BusinessException，由 GlobalExceptionHandler 全局兜住。
 * 第8课：菜谱上账——验菜取价全部查数据库 t_menu。
 * 第9课：服务员瘦身——下单的全部业务搬进 OrderService（收银台），
 *        Controller 只管 HTTP 进出：接单 → 调收银台 → 回话，共一行。
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

    // 点单：对照第2课——那时这里堆着几十行验菜算账炒菜的业务代码，
    // 现在只剩"转交给收银台"一行。业务逻辑放 Service 层，
    // Controller 可测试、可替换，将来 AI 点单（第18课）也能复用同一个收银台
    @PostMapping("/order")
    public Result<OrderResponse> order(@RequestBody OrderRequest orderRequest) {
        return Result.success(orderService.createOrder(orderRequest));
    }
}
