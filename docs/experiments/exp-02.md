# 实验02 Git 版本控制与团队协作

实验日期：2026-11-07。仓库：<https://github.com/NONAME114541/javawebframework>。

## 基线与个人分支

从课程项目现有 main（689bf6e）开始，新增 github 远程，保留原 Gitee origin。
创建 exp-02-work，将 application.yml 的 server.port 从 8080 改为 8081。
提交：2fbdc5b，config: 调整实验环境服务端口为 8081。

PR #1：<https://github.com/NONAME114541/javawebframework/pull/1>。
核对单文件差异并运行 Maven 测试后，提交自查评论并完成合并，合并提交 9f15210。
本次只有一个 GitHub 账号，评论属于自查，不是另一位真实成员的独立 Approve。
如课程要求他人批准，需邀请实际同学使用自己的账号审核。

## 模拟成员修改与真实冲突

两个模拟分支从 9f15210 创建，均为单账号模拟，不冒充其他人。

| 模拟分支 | 不同位置修改 | 同一位置修改 | 提交 |
| --- | --- | --- | --- |
| exp-02-member-a | 为 datasource.username 补充 H2 默认用户说明 | 端口改为 ${SERVER_PORT:8081} | 803c1e7 |
| exp-02-member-b | 为 storage.dir 补充上传目录说明 | 端口改为 ${SERVER_PORT:8082} | e0c7cca |

在成员 A 分支执行：

```sh
git merge --no-ff exp-02-member-b
```

Git 返回 CONFLICT (content)，git status --short 显示 UU src/main/resources/application.yml。
不同位置的注释自动合并；同一端口位置产生真实冲突：

```text
<<<<<<< HEAD
  port: ${SERVER_PORT:8081}
=======
  port: ${SERVER_PORT:8082}
>>>>>>> exp-02-member-b
```

手工删除标记并选择 ${SERVER_PORT:8081}，保留双方注释。
保留 8081 是为了与 PR #1 的实验端口一致，同时允许用 SERVER_PORT 环境变量覆盖。
没有直接选用整份 ours/theirs 文件，以免丢失另一分支在不同位置的修改。

```sh
git diff --check
git add src/main/resources/application.yml
git commit -m "merge: 手工解决模拟成员端口冲突并保留双方说明"
git log --graph --oneline --decorate -6
```

合并提交 e44014d 的两个父提交为 803c1e7 和 e0c7cca，分支历史完整保留。
PR #2：<https://github.com/NONAME114541/javawebframework/pull/2>，用于合并冲突解决结果及本记录。

## 验证结果

```sh
mvnw.cmd -B -ntp test
mvnw.cmd -B -ntp spring-boot:run
```

- 最终配置测试：Tests run: 1, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。
- 应用实际在 8081 端口启动，数据库使用项目默认 H2 内存库；本次未切换 MySQL。
- GET /hello：HTTP 200，code=200，data 为“南坡小炒，本店开业！”。
- GET /menu：HTTP 200，code=200，返回 6 道初始化菜品。
- GET /actuator/health：HTTP 200，status=UP。
- git diff --check 无错误；解决后文件中无冲突标记。

本实验文档未另附课堂接口清单或课后题，以上是实际执行的配置回归检查，不宣称完成未提供的题目。

## 网络问题与同步方法

Git HTTPS push/fetch 多次出现连接重置或连接超时。GitHub 官方 API 连接正常，
因此通过 GitHub Git 数据 API 上传原有 blob、tree、commit 后更新实验分支。
逐项核对远程对象 SHA 与本地对象完全一致，不重写原提交、不强制覆盖远程分支。
GitHub 的签名合并提交也通过 API 读取并核对完整 SHA 后同步本地。

最后将合并后的 main 标记为 exp-02 并同步远程。标签与提交记录可在仓库页面核验。
截图和完整原始日志另存于本次提交材料中；日志浏览器截图明确标注为原始日志展示，不伪装成终端界面。
