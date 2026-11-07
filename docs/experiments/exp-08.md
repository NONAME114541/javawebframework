# 实验08 Spring AOP 与事务管理

基于实验07分支继续实现；分支 exp-08-work，标签 exp-08。

## 实现

- 使用 Boot 4.1.1 BOM 中的 `spring-boot-starter-aspectj`。
- `ApiLoggingAspect` 通过 `@Aspect`、`@Around` 拦截 Controller，记录 UTC 调用时间、方法、参数、返回结果和 elapsedMs。密码及访问/刷新令牌脱敏，异常记录类型并重新抛出。
- 新增 `t_user_role`（用户角色分配），有 `user_id` 外键与 `(user_id, role_name)` 唯一约束。
- 原 `/register` 委托 `RegistrationService.register`。该方法以 `@Transactional(rollbackFor=Exception.class)` 包含用户创建与默认 USER 角色分配。
- `UserRoleService` 是独立 Bean，分别提供 REQUIRED 与 REQUIRES_NEW 方法。跨 Bean 调用经过事务代理，避免同类自调用绕过代理。
- 故障注入服务和 `/tx-demo/**` 接口只在 exp08 profile 下开放。

## 验证步骤

```powershell
.\mvnw.cmd -B -ntp test
.\mvnw.cmd -B -ntp spring-boot:run '-Dspring-boot.run.profiles=exp08'
```

数据库沿用项目 H2 内存库，端口8081。重启后先注册演示用户。
HTTP验证脚本顺序覆盖正常注册、登录回归、唯一约束异常、REQUIRED及REQUIRES_NEW。

## 验证结果

| 场景 | 外层新用户 | 外层默认角色 | 内层新增角色 |
| --- | --- | --- | --- |
| 正常注册 | 1 | 1 | 不适用 |
| 重复 USER 角色唯一约束异常 | 0 | 0 | 0 |
| REQUIRED后外层抛异常 | 0 | 0 | 0 |
| REQUIRES_NEW后外层抛异常 | 0 | 0 | 1 |

故障并非只抛一个模拟异常：注册已向用户表和角色表 flush，再次插入同用户的 USER 角色触发 H2 SQLState 23505，异常后两表计数均恢复。集成测试同时核对两表总行数前后不变。

传播对比中的内层角色关联预先提交的现有用户。独立事务不能依赖外层尚未提交的新用户，外键可见性会影响实验。因此外层创建新用户与默认角色，内层对现有用户分配演示角色，随后外层统一抛异常。

Maven共10个测试全部通过（6项AOP/事务集成测试、3项IoC测试、1项原有上下文测试），0失败、0错误。
测试方法没有加@Transactional，断言读取的是业务事务完成后的数据库状态，避免测试自身回滚掩盖真实结果。

`@Around` 测试还确认参数、结果、耗时存在，密码和两种令牌原文未出现在日志中。异常继续抛出，数据库事务回滚不被切面吞掉。

真实 HTTP 结果与集成测试一致。沿用项目原异常处理约定：故障请求 HTTP 状态仍为200，JSON业务码为50001；报告据实区分两者，没有将该响应写成HTTP500。

## 回滚条件与常见问题

默认声明式事务对 RuntimeException/Error 回滚；此注册方法显式设置 rollbackFor=Exception.class，以包含受检异常。不能只在同一类内调用事务方法期待新传播行为；本实验使用独立注入服务。
不要在切面或业务方法中吞异常后返回成功；需要让事务拦截器观察到异常，或明确设置rollback-only。
REQUIRES_NEW挂起外层事务、开启独立事务，需要额外连接，不能把它当作两表原子注册的默认选项。

截图使用 VS Code 的实际源码和实际运行后保存的日志；报告将图文对应。完整测试、运行、HTTP输出保留在提交材料中。
未单独提供课后题，不宣称完成未知题目。
