package com.cdutetc.nanpo.service;

/**
 * 墩子（配菜员）：只负责配菜这一件事——单一职责。
 * 第3课：暂时还是普通 Java 类，由服务员自己 new 出来（第4课交给容器管理）。
 */
public class SideDishService {

    public void prepareSideMaterial(String dishName, int count) {
        System.out.printf("[墩子] %s x %d 配菜完成%n", dishName, count);
    }
}
