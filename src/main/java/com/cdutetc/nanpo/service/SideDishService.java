package com.cdutetc.nanpo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 墩子（配菜员）：只负责配菜这一件事——单一职责。
 * 第3课：普通 Java 类，由服务员手动 new。
 * 第4课：加上 @Service，人事权上交——由 IoC 容器创建和管理。
 * 第11课：加课堂演示开关 demo.kitchen-failure——启动时传
 *        -Ddemo.kitchen-failure=true，墩子配菜就抛异常（模拟厨房着火），
 *        用来演示"订单已落库、后续步骤失败"时事务如何收拾残局。
 */
@Service
public class SideDishService {

    // 报到日志：证明 Bean 在容器启动时就被创建（而非第一次请求时才创建）
    public SideDishService() {
        System.out.println("[墩子] 向容器报到，单例就位：" + Integer.toHexString(System.identityHashCode(this)));
    }

    @Value("${demo.kitchen-failure:false}")
    private boolean kitchenFailure;

    public void prepareSideMaterial(String dishName, int count) {
        if (kitchenFailure) {
            // 系统级异常（不是顾客的锅）→ 走第6课 GlobalExceptionHandler 的兜底分支 {code:50001}
            throw new IllegalStateException("厨房着火了！配菜失败：" + dishName);
        }
        System.out.printf("[墩子] %s x %d 配菜完成%n", dishName, count);
    }
}
