package com.cdutetc.nanpo.service;

import org.springframework.stereotype.Service;

/**
 * 墩子（配菜员）：只负责配菜这一件事——单一职责。
 * 第3课：普通 Java 类，由服务员手动 new。
 * 第4课：加上 @Service，人事权上交——由 IoC 容器创建和管理，
 *        启动时实例化单例，等着被容器"派"到需要的地方（依赖注入）。
 */
@Service
public class SideDishService {

    // 报到日志：证明 Bean 在容器启动时就被创建（而不是第一次请求时才创建），
    // 且全店只此一份（单例），多个使用者共享同一个对象
    public SideDishService() {
        System.out.println("[墩子] 向容器报到，单例就位：" + Integer.toHexString(System.identityHashCode(this)));
    }

    public void prepareSideMaterial(String dishName, int count) {
        System.out.printf("[墩子] %s x %d 配菜完成%n", dishName, count);
    }
}
