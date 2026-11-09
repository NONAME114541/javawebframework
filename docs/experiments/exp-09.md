# 实验09 Spring MVC 与 RESTful API

以菜品资源实现 `/api/menus` 五件套：GET列表、GET详情、POST创建、PUT修改、DELETE删除；批量删除使用 `DELETE /api/menus?ids=1,2`。

绑定方式：`@PathVariable Long id`、`@RequestParam page/size/keyword`、`@RequestBody MenuRequest`、`@RequestParam List<Long> ids`。

所有接口返回 `Result{code,msg,data}`；创建成功额外返回 HTTP 201 与 Location。参数类型、缺失参数、JSON错误及业务取值错误统一为 code=40001。

错误URL改正：

1. `GET /getMenuById?id=1` → `GET /api/menus/1`
2. `POST /createMenu` → `POST /api/menus`
3. `DELETE /deleteMenu?id=1` → `DELETE /api/menus/1`

完整请求见 `docs/experiments/exp-09.http`。测试覆盖CRUD、分页筛选、四种绑定、批量删除、错误绑定、三个错误URL与统一响应。
