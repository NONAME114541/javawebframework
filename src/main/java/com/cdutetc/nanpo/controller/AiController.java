package com.cdutetc.nanpo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dto.ChatRequest;
import com.cdutetc.nanpo.service.AiService;
import com.cdutetc.nanpo.vo.OrderVO;

/**
 * AI 服务员窗口。
 * 第17课：POST /ai/chat 和小智聊天（公开——聊天自由）。
 * 第18课：POST /order/ai 让小智帮你下单——注意路径在 /order/** 下，
 *        按第16课的规矩：动真格下单必须亮卡（拦截器自动生效）。
 */
@RestController
public class AiController {

    @Autowired
    private AiService aiService;

    // 和 AI 服务员聊天（问菜单会触发工具调用查真实菜单）
    @PostMapping("/ai/chat")
    public Result<String> chat(@RequestBody ChatRequest req) {
        return Result.success(aiService.chat(req.getMessage()));
    }

    // AI 帮你下单：说句话就行——"来份不辣的下饭菜"
    @PostMapping("/order/ai")
    public Result<OrderVO> orderByAi(@RequestBody ChatRequest req) {
        return Result.success(aiService.orderByAi(req.getMessage()));
    }
}
