package com.cdutetc.nanpo.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cdutetc.nanpo.service.MenuTool;
import com.cdutetc.nanpo.service.OrderTool;

/**
 * MCP（Model Context Protocol）服务配置（第20课彩蛋）。
 * 把本店的工具注册到 MCP Server——任何支持 MCP 的 AI 客户端
 * （Claude Desktop / Cursor / 各类 MCP 客户端）都能连进来：
 * 查菜单（MenuTool，第18课旧代码原样复用）、下订单（OrderTool，本请新增）。
 * 这就是"AI 客户端真的进来点菜"：你的系统成了大模型的工具。
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider nanpoTools(MenuTool menuTool, OrderTool orderTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(menuTool, orderTool)
                .build();
    }
}
