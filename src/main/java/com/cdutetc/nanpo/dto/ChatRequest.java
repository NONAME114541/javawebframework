package com.cdutetc.nanpo.dto;

import lombok.Data;

/** AI 对话请求（第17课）：{ "message": "有什么不辣的菜？" } */
@Data
public class ChatRequest {

    private String message;
}
