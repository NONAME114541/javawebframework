package com.cdutetc.nanpo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.common.UserContext;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.dao.OrderRepository;
import com.cdutetc.nanpo.dto.OrderRequest;
import com.cdutetc.nanpo.entity.Menu;
import com.cdutetc.nanpo.entity.Order;
import com.cdutetc.nanpo.entity.OrderItem;
import com.cdutetc.nanpo.vo.OrderPageVO;
import com.cdutetc.nanpo.vo.OrderVO;

/**
 * 收银台（订单业务服务）。
 * 第9课新增：验菜算价、编排厨房、订单落库的业务全部从 Controller 搬到这里。
 * 第10课改造：入参用 dto.OrderRequest、出参用 vo.OrderVO。
 * 第11课调整：先落单、再叫厨房（贴近真实饭店流程）——由此出现
 * "单已落库、厨房着火"的窗口，@Transactional 让这种时候数据库不留下半条订单。
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

    // 第11课主角：事务边界放在业务方法上——方法内所有数据库操作同生共死，
    // 中途任何一步抛异常，已做的操作全部回滚（实验对照：不加它，厨房着火后
    // t_order 里会残留一条"钱收了、菜没炒"的脏订单）。
    // 两个经典失效坑（课堂口头展开）：同类内部自调用、非 public 方法
    @Transactional
    public OrderVO createOrder(OrderRequest request) {
        // 第16课：查票员(拦截器)放进来的当前会员——订单记名
        UserContext.CurrentUser current = UserContext.require();

        // ---- 第一步：落单（验菜 → 算价 → 明细挂单 → 入库）----
        Order order = new Order();
        order.setCreateTime(LocalDateTime.now());
        order.setUserId(current.userId());
        order.setUsername(current.username());
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.DishCount dc : request.getDishList()) {
            Menu menu = menuRepository.findByDishName(dc.getDishName())
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND));

            BigDecimal subTotal = menu.getPrice().multiply(BigDecimal.valueOf(dc.getCount()));
            total = total.add(subTotal);
            order.addOrderItem(new OrderItem(dc.getDishName(), dc.getCount(), menu.getPrice(), subTotal));
        }

        order.setTotalAmount(total);
        // cascade=ALL：一次 save，主表明细全部入库
        Order saved = orderRepository.save(order);

        // ---- 第二步：叫厨房起菜 ----
        // （第11课调整：从"边点边炒"改为"先落单、后叫厨房"——真实饭店就是这么干的，
        // 同时也制造了事务教学场景：如果厨房在这里着火，刚落库的订单怎么办？）
        for (OrderItem item : saved.getOrderItems()) {
            sideDishService.prepareSideMaterial(item.getDishName(), item.getCount());
            cookService.cookDish(item.getDishName(), item.getCount());
        }

        OrderVO vo = OrderVO.from(saved);
        vo.setMsg("下单成功，请到取餐口稍等～（订单号 " + saved.getId() + "）");
        return vo;
    }

    // ---- 第12课：翻账本（分页/详情/联表查询）----

    /** 分页查订单：页号从 0 开始，按下单时间倒序（最新在前） */
    public OrderPageVO getOrderPage(int page, int size) {
        Page<Order> orderPage = orderRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")));

        // Page → VO 的组装放 Service（VO 不沾框架类型）
        OrderPageVO vo = new OrderPageVO();
        vo.setRecords(orderPage.getContent().stream().map(OrderVO::from).toList());
        vo.setTotalElements(orderPage.getTotalElements());
        vo.setTotalPages(orderPage.getTotalPages());
        vo.setPageNum(orderPage.getNumber());
        vo.setPageSize(orderPage.getSize());
        return vo;
    }

    /** 订单详情：查不到抛业务异常（复用第6课的处理台） */
    public OrderVO getOrder(Long id) {
        return OrderVO.from(orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.ORDER_NOT_FOUND)));
    }

    /** "谁点过这道菜"：JPQL 跨 t_order/t_order_item 联表查询 */
    public List<OrderVO> searchByDish(String dishName) {
        return orderRepository.findByDishName(dishName).stream()
                .map(OrderVO::from)
                .toList();
    }
}
