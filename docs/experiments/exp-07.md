# 实验07 Spring IoC 与依赖注入

日期：2026-11-07。基于现有南坡小炒项目继续开发，保留此前 exp-02 的历史与配置。

## 实现

- `UserService` 保留 `@Service`；新增 `@Repository UserMapper`，内部委托现有 `UserRepository` 查询。
- 用 `@Autowired setUserMapper(...)` 演示 setter 注入，查询方法沿 `Controller → UserService → UserMapper → UserRepository → H2` 执行。
- 保留原有构造器注入（`UserRepository`、`JwtUtil`），单一构造器可以省略 `@Autowired`；现有 Controller 也展示了字段注入。
- 查询返回 `UserVO(id, username, nickname)`，不序列化用户实体和密码哈希。
- `UserService` 加入 `jakarta.annotation.PostConstruct`、`PreDestroy` 回调。
- `/ioc/**` 诊断 Controller 仅在 `exp07` profile 下注册。

## 运行

```powershell
.\mvnw.cmd -B -ntp test
.\mvnw.cmd -B -ntp spring-boot:run '-Dspring-boot.run.profiles=exp07'
```

应用沿用默认端口 8081、H2 内存库。先调用现有 `/register` 注册演示用户，再进行查询，重启应用后需重新注册。
可执行提交材料中的 `验证脚本.py`，或使用同目录的 `exp-07.http`。

## 验证清单

| 项目 | 实际结果 |
| --- | --- |
| Bean 与注入查询 | 真实 Spring 容器解析 `@Service`、`@Repository`、`@Autowired`；查询返回预期公开字段 |
| HTTP 注册与登录回归 | `/register`、`/login` 返回 code=200；令牌未写入截图或日志 |
| HTTP 用户查询 | `/ioc/users/exp07-demo` 返回 HTTP 200，昵称为“IOC实验用户”，无 password 字段 |
| 用户不存在 | `/ioc/users/missing-exp07` 返回 HTTP 404、code=404 |
| singleton | 同容器 3 次 `getBean` 引用 `==`，scope=true，与 Controller 注入引用相同 |
| 跨请求复用 | 两次 HTTP 请求对象标识相同 |
| 容器边界 | 两个独立 ApplicationContext 中同类型 Bean 不是同一个实例 |
| 生命周期 | 构造 → `@Autowired` 注入 → `@PostConstruct` → `context.close()` 触发 `@PreDestroy` |
| 测试总数 | 4 个测试（新增 3 个 + 原有 1 个），0 失败、0 错误，BUILD SUCCESS |

引用展示示例 `UserService@329e6700` 是 `System.identityHashCode` 标识，不是 JVM 物理地址。
结论以引用 `==` 和容器 scope 检查为准，不能仅靠 hash 相同认定单例。
默认 singleton 的范围是同一容器、同一个 Bean 定义，不是全 JVM 全局只有一个对象。

生命周期测试使用真实 `AnnotationConfigApplicationContext`，只模拟外围 Repository/JWT 依赖。
构造、注入、初始化与销毁由 Spring 执行，测试没有手工调用这些回调。
真实 HTTP 查询则通过项目原有 H2/JPA 执行，不使用模拟数据库结果。

## 文件与提交

源码：`dao/UserMapper.java`、`service/UserService.java`、`vo/UserVO.java`、`controller/IocExperimentController.java`。
测试：`src/test/java/com/cdutetc/nanpo/IocExperimentTests.java`。
本实验提交到 `exp-07-work`，打附注标签 `exp-07`，原 main 与 exp-02 标签保留。
报告与真实浏览器截图另附；日志截图明确标记为原始日志展示。
未提供单独的课堂课后题，不能把额外作业记作已完成。
