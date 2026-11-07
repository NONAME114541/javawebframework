package com.cdutetc.nanpo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.service.UserService;
import com.cdutetc.nanpo.vo.UserVO;

/** 实验诊断端点仅在 exp07 profile 下开放。 */
@Profile("exp07")
@RestController
@RequestMapping("/ioc")
public class IocExperimentController {

    private final UserService userService;
    private final ApplicationContext context;

    // 单一构造器可以省略 @Autowired；Service 的 setter 显式演示 @Autowired。
    public IocExperimentController(UserService userService, ApplicationContext context) {
        this.userService = userService;
        this.context = context;
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<Result<UserVO>> findUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(Result.success(user)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Result.fail(404, "用户不存在")));
    }

    @GetMapping("/singleton")
    public Result<Map<String, Object>> singleton() {
        UserService first = context.getBean(UserService.class);
        UserService second = context.getBean(UserService.class);
        UserService third = context.getBean(UserService.class);
        return Result.success(Map.of(
                "beanName", "userService",
                "singletonScope", context.isSingleton("userService"),
                "identities", List.of(identity(first), identity(second), identity(third)),
                "sameInstance", first == second && second == third,
                "injectedMatches", first == userService));
    }

    // identityHashCode 是引用标识的展示，并非 JVM 物理内存地址；结论以 == 为准。
    private String identity(Object value) {
        return value.getClass().getSimpleName() + "@"
                + Integer.toHexString(System.identityHashCode(value));
    }
}
