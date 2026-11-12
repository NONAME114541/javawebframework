# 实验10 统一异常处理与参数校验

## 实现内容

- `RegisterRequest` 使用 `@NotNull`、`@Size`、`@Pattern` 完成字段校验。
- `/register` 使用 `@Valid` 触发 Bean Validation。
- 用户表增加手机号字段，注册时检查手机号是否已存在。
- `GlobalExceptionHandler` 分别处理参数校验、业务异常和系统异常。
- 所有响应继续使用 `Result` 的 `code`、`msg`、`data` 结构。

## 验证场景

1. 正常注册返回 `code=200`。
2. 用户名过短、手机号格式错误返回 `code=40001`，`data` 包含字段错误。
3. 用户名缺失返回 `code=40001`，`data.username=用户名不能为空`。
4. 重复手机号返回业务错误 `code=40409`。
5. 未知系统异常返回通用错误 `code=50001`，不泄露内部异常信息。

自动化测试类：`ValidationExceptionTests`。
