package com.cdutetc.nanpo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.dto.OrderRequest;
import com.cdutetc.nanpo.vo.OrderVO;

/**
 * AI 服务员"小智"。
 * 第17课：基于 Spring AI 的聊天（ChatClient + 人设提示词）。
 * 第18课两项新能力：
 *   ① 工具调用——chatClient 挂上 MenuTool，小智能查"真菜单"回答问题；
 *   ② 结构化输出——orderParser 把"来份不辣的下饭菜"直接解析成 OrderRequest 对象，
 *      随即调用第9课的 OrderService.createOrder()：AI 下单复用全部旧业务，一行不改。
 */
@Service
public class AiService {

    /** 聊天客户端：小智人设 + 带菜单工具（defaultTools——构建期注册，次次可用） */
    private final ChatClient chatClient;

    /** 点单解析客户端：另一个人设（无工具），专职把自然语言变成订单 JSON */
    private final ChatClient orderParser;

    private final OrderService orderService;

    public AiService(ChatClient.Builder chatClientBuilder, MenuTool menuTool, OrderService orderService) {
        this.orderService = orderService;

        // Builder 是可复用的：clone() 出独立副本，各自设置人设，互不串台
        this.chatClient = chatClientBuilder.clone()
                .defaultSystem("你是成都理工大学工程技术学院南坡小炒饭馆的AI服务员，名叫小智。" +
                        "你熟悉川菜家常菜，回答热情简短（两三句话），最后可以自然地引导顾客点单。" +
                        "顾客问菜单相关问题时，先调用工具查真实菜单再回答，不要凭空编造菜品和价格。" +
                        "如果顾客的问题与饭馆无关，礼貌地把话题拉回点餐。")
                .defaultTools(menuTool)
                .build();

        this.orderParser = chatClientBuilder.clone()
                .defaultSystem("你是南坡小炒的点单解析器，只输出 JSON，不输出任何多余文字。" +
                        "把顾客的话解析成点单结构：dishList 里每项是 {\"dishName\":菜名字符串, \"count\":正整数份数}。" +
                        "菜名必须使用菜单上的原样名称（如：回锅肉、米饭）；顾客没说份数默认 1 份；" +
                        "实在识别不出任何菜品时输出空 dishList。")
                .build();
    }

    /** 聊天：一问一答；问菜单会自动触发 MenuTool 工具调用 */
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

    /**
     * AI 点单（第18课核心）：自然语言 → OrderRequest → 直接走第9课的收银台。
     * 结构化输出 .entity(OrderRequest.class)：框架自动在提示词里附上目标 JSON 结构，
     * 模型照格式输出，框架再反序列化成对象——前后端第2课定的契约，AI 也照办。
     */
    public OrderVO orderByAi(String message) {
        OrderRequest request;
        try {
            request = orderParser.prompt()
                    .user(message)
                    .call()
                    .entity(OrderRequest.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_ERROR);
        }
        if (request == null || request.getDishList() == null || request.getDishList().isEmpty()) {
            // 识别不出菜品：AI 的"幻觉"被第6课+第8课的业务校验兜住
            throw new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND);
        }
        // 关键一行：AI 只是新的"入口"，下单仍是老收银台——
        // 事务、价格快照、订单记名（第9/11/16课）全部原样生效
        return orderService.createOrder(request);
    }
}
