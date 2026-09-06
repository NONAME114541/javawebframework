package com.cdutetc.nanpo.service;

/**
 * 厨师：只负责炒菜这一件事——单一职责。
 * 第3课：暂时还是普通 Java 类，由服务员自己 new 出来（第4课交给容器管理）。
 */
public class CookService {

    public void cookDish(String dishName, int count) {
        System.out.printf("[厨师] %s x %d 炒菜完成%n", dishName, count);
    }
}
