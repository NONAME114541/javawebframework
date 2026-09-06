package com.cdutetc.nanpo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.dao.OrderRepository;
import com.cdutetc.nanpo.entity.Menu;
import com.cdutetc.nanpo.entity.Order;
import com.cdutetc.nanpo.pojo.OrderItem;
import com.cdutetc.nanpo.pojo.OrderRequest;
import com.cdutetc.nanpo.pojo.OrderResponse;

/**
 * 收银台（订单业务服务）。
 * 第9课新增：服务员身上"验菜算价、编排厨房、订单落库"的全部业务搬到这里——
 * Controller 只管 HTTP 进出，业务编排属于 Service 层。
 */
@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final SideDishService sideDishService;
    private final CookService cookService;

    // 构造器注入：第8课在 MenuDataInitializer 里示范过，本次推广为常规写法
    public OrderService(MenuRepository menuRepository,
                        OrderRepository orderRepository,
                        SideDishService sideDishService,
                        CookService cookService) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.sideDishService = sideDishService;
        this.cookService = cookService;
    }

    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCreateTime(LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO;

        // 小票侧：给前端看的明细（第2课以来的 pojo.OrderItem）
        List<OrderItem> receiptItems = new ArrayList<>();

        for (OrderRequest.DishCount dc : request.getDishList()) {
            String dishName = dc.getDishName();
            Integer count = dc.getCount();

            // 验菜+取价（第8课已改为查库）
            Menu menu = menuRepository.findByDishName(dishName)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND));

            BigDecimal subTotal = menu.getPrice().multiply(BigDecimal.valueOf(count));
            total = total.add(subTotal);

            // 账本侧：明细实体挂上订单（unitPrice 是价格快照）。
            // 注意这里必须写全限定名：entity.OrderItem 与 pojo.OrderItem 同名，
            // 同一个文件里 import 不了两个——这个别扭正是第10课
            // "小票与账本分离(DTO/VO)"要解决的
            com.cdutetc.nanpo.entity.OrderItem item =
                    new com.cdutetc.nanpo.entity.OrderItem(dishName, count, menu.getPrice(), subTotal);
            order.addOrderItem(item);

            // 小票侧：并行拼一份前端明细
            OrderItem receiptItem = new OrderItem();
            receiptItem.setDishName(dishName);
            receiptItem.setCount(count);
            receiptItem.setUnitPrice(menu.getPrice());
            receiptItem.setSubTotal(subTotal);
            receiptItems.add(receiptItem);

            // 厨房编排（第3课的调用链从 Controller 搬进业务层）
            sideDishService.prepareSideMaterial(dishName, count);
            cookService.cookDish(dishName, count);
        }

        order.setTotalAmount(total);
        // cascade=ALL：一次 save，主表明细全部入库，order_id 自动回填
        Order saved = orderRepository.save(order);

        OrderResponse resp = new OrderResponse();
        resp.setItemList(receiptItems);
        resp.setTotalMoney(saved.getTotalAmount());
        // 带上数据库生成的订单号——前端肉眼可见"真的入库了"
        resp.setMsg("下单成功，请到取餐口稍等～（订单号 " + saved.getId() + "）");
        return resp;
    }
}
