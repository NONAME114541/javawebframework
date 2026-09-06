package com.cdutetc.nanpo.common;

/**
 * 当前登录人上下文（第16课）。
 * 拦截器查完票把"当前会员"放进 ThreadLocal——
 * 同一个请求线程里，后续任何 Service 都能直接取，不用层层传参。
 *
 * 不清理会串号：Tomcat 线程池复用线程，上一个请求的登录人
 * 可能残留给下一个请求——所以拦截器 afterCompletion 必须 clear()。
 */
public class UserContext {

    /** 第10课预告过的 record：一行定义不可变数据类 */
    public record CurrentUser(Long userId, String username) {
    }

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 需要登录人的地方用它取——没登录直接抛业务异常 */
    public static CurrentUser require() {
        CurrentUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.TOKEN_MISSING);
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
