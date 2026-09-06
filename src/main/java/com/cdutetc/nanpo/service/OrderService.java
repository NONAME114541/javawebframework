package com.cdutetc.nanpo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.dao.OrderRepository;
import com.cdutetc.nanpo.dto.OrderRequest;
import com.cdutetc.nanpo.entity.Menu;
import com.cdutetc.nanpo.entity.Order;
import com.cdutetc.nanpo.entity.OrderItem;
import com.cdutetc.nanpo.vo.OrderVO;

/**
 * 收银台（订单业务服务）。
 * 第9课新增：验菜算价、编排厨房、订单落库的业务全部从 Controller 搬到这里。
 * 第10课改造：入参用 dto.OrderRequest、出参用 vo.OrderVO——
 * 服务层内部只见实体（账本），对入口出口只见 DTO/VO（单据）。
 */
@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final SideDishService sideDishService;
    private final CookService cookService;

    public OrderService(MenuRepository menuRepository,
                        OrderRepository orderRepository,
                        SideDishService sideDishService,
                        CookService cookService) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.sideDishService = sideDishService;
        this.cookService = cookService;
    }

    public OrderVO createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCreateTime(LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.DishCount dc : request.getDishList()) {
            String dishName = dc.getDishName();
            Integer count = dc.getCount();

            // 验菜+取价（查库）
            Menu menu = menuRepository.findByDishName(dishName)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND));

            BigDecimal subTotal = menu.getPrice().multiply(BigDecimal.valueOf(count));
            total = total.add(subTotal);

            // 账本侧：明细实体挂上订单（unitPrice 价格快照）。
            // 第10课收益：pojo 退役后，entity.OrderItem 可以正常 import——
            // 第9课里那行全限定名的别扭不复存在
            order.addOrderItem(new OrderItem(dishName, count, menu.getPrice(), subTotal));

            // 厨房编排
            sideDishService.prepareSideMaterial(dishName, count);
            cookService.cookDish(dishName, count);
        }

        order.setTotalAmount(total);
        // cascade=ALL：一次 save，主表明细全部入库
        Order saved = orderRepository.save(order);

        // 小票侧：账本 → 小票一把转换（OrderVO.from 内部完成明细级转换）
        OrderVO vo = OrderVO.from(saved);
        vo.setMsg("下单成功，请到取餐口稍等～（订单号 " + saved.getId() + "）");
        return vo;
    }
}
