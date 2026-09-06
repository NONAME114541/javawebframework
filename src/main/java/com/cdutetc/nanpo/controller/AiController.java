package com.cdutetc.nanpo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dto.ChatRequest;
import com.cdutetc.nanpo.service.AiService;

/**
 * AI 服务员窗口（第17课）：和小智聊天。
 * 公开接口——聊天自由（第16课的规矩：逛店聊天随便，动真格下单才要亮卡）。
 */
@RestController
public class AiController {

    @Autowired
    private AiService aiService;

    // 和 AI 服务员聊天
    @PostMapping("/ai/chat")
    public Result<String> chat(@RequestBody ChatRequest req) {
        return Result.success(aiService.chat(req.getMessage()));
    }
}
