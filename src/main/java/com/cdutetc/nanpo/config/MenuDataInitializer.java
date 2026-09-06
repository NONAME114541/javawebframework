package com.cdutetc.nanpo.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.entity.Menu;

/**
 * 开市前的准备：把初始菜单装进数据库。
 * 时机：CommandLineRunner——容器启动完成、表已建好之后自动执行。
 * 幂等：先 count() 判断，已有数据就不重复插（第19课换 MySQL 重启时菜单不会翻倍）。
 */
@Component
public class MenuDataInitializer implements CommandLineRunner {

    // 推荐写法：构造器注入（字段可 final；单构造器时可省 @Autowired）
    private final MenuRepository menuRepository;

    public MenuDataInitializer(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public void run(String... args) {
        if (menuRepository.count() > 0) {
            return;
        }
        menuRepository.save(new Menu("回锅肉", new BigDecimal("18")));
        menuRepository.save(new Menu("鱼香肉丝", new BigDecimal("16")));
        menuRepository.save(new Menu("麻婆豆腐", new BigDecimal("12")));
        menuRepository.save(new Menu("番茄炒蛋", new BigDecimal("10")));
        menuRepository.save(new Menu("蛋炒饭", new BigDecimal("8")));
        menuRepository.save(new Menu("米饭", new BigDecimal("1")));
    }
}
