package com.cdutetc.nanpo.service;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.vo.MenuVO;

/**
 * 给 AI 用的工具箱（第18课）：Tool Calling——
 * 大模型自己"不会"查数据库，但可以调用我们注册的工具方法。
 * 工具内部就是第8课的 findAll——AI 和真人顾客走同一套代码、同一个库。
 */
@Component
public class MenuTool {

    @Autowired
    private MenuRepository menuRepository;

    @Tool(description = "获取本店当前完整菜单（菜名、价格、照片路径）")
    public List<MenuVO> getMenu() {
        return menuRepository.findAll().stream().map(MenuVO::from).toList();
    }
}
