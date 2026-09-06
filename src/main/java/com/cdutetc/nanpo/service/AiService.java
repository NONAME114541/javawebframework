package com.cdutetc.nanpo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;

/**
 * AI 服务员"小智"（第17课）：基于 Spring AI 的聊天服务。
 * ChatClient.Builder 由 spring-ai-starter-model-openai 自动装配——
 * 端点/模型/Key 全部来自 yml 的 spring.ai.openai.* 配置，换模型只改配置。
 */
@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient.Builder chatClientBuilder) {
        // 系统提示词：给 AI 定"人设"——南坡小炒的服务员小智
        // （Spring AI 2.0 的 Builder 方法叫 defaultSystem——设置默认系统提示词）
        this.chatClient = chatClientBuilder
                .defaultSystem("你是成都理工大学工程技术学院南坡小炒饭馆的AI服务员，名叫小智。" +
                        "你熟悉川菜家常菜，回答热情简短（两三句话），最后可以自然地引导顾客点单。" +
                        "如果顾客的问题与饭馆无关，礼貌地把话题拉回点餐。")
                .build();
    }

    /** 聊天：一问一答（单轮；多轮记忆留作拓展） */
    public String chat(String message) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            // 无 Key/网络不通/额度用尽——统一转成友好提示，不让异常栈吓到顾客
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_ERROR);
        }
    }
}
