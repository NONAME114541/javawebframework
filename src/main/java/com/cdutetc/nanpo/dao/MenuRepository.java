package com.cdutetc.nanpo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdutetc.nanpo.entity.Menu;

/**
 * 菜单的数据访问接口——操作 t_menu 的"手"。
 * 零实现类：Spring Data JPA 启动时自动生成；
 * 零 SQL：findByDishName 按方法名规则自动派生查询
 * （select * from t_menu where dish_name = ?）。
 */
public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByDishName(String dishName);
}
