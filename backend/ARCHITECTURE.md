# 后端分层与手写指南

当前结构将原来的 `business` 数据访问层改名为 `repository`，并把其中的业务流程移到 `service/workflow`。Controller 接口、业务判断、异常消息、数据库 SQL 和返回结构保持原有约定。

## 调用关系

```text
Controller
  └─ Service（接口入口、缓存、业务协调）
       ├─ Workflow（Service 层内部的复杂流程、权限判断、事务、文件处理）
       │    └─ Repository（数据查询和写入）
       └─ Repository（简单数据查询和写入）
              └─ Mapper（MyBatis-Plus CRUD / 注解 SQL / XML SQL）
                   └─ 数据库
```

`Workflow` 是 Service 层内部的组件，不是另一种数据访问层。简单用例可以直接在 Service 中调用 Repository，不必为每个方法再增加 Workflow。

## 职责约定

| 位置 | 应放的代码 | 不应放的代码 |
|---|---|---|
| `controller` | HTTP 参数、请求格式校验、当前用户、响应包装 | Wrapper、Mapper 调用、业务流程 |
| `service/impl` | 对外用例、缓存入口、业务协调、结果组装 | SQL 和 Wrapper、直接调用 Mapper |
| `service/workflow` | 权限与业务规则、多步事务、文件处理和补偿、VO 组装 | SQL 和 Wrapper、直接调用 Mapper |
| `repository` | 数据访问接口、按需求命名的查询方法 | 登录判断、权限拒绝、HTTP 错误码、上传文件、完整业务流程 |
| `repository/impl` | Wrapper、调用 Mapper、数据库更新表达式 | Service 调用、业务异常、文件处理 |
| `mapper` / XML | SQL、参数绑定、查询结果映射 | 业务编排、权限错误响应、文件处理 |

Repository 可以用用户 ID 过滤数据，例如 `findByOwner(queueId, userId)`。查询无结果时应该返回查询结果，由 Service/Workflow 决定应报“不存在”还是“无权操作”。

Repository 可以返回由 SQL 映射的查询投影，例如播放历史 VO；已有 XML 的 `resultMap` 无需为了分层而重写。默认值、错误判断、多个查询结果的业务组装则留在 Service 层。

## 为什么保留 Workflow

原来的多个 Service 在外层处理缓存，Business 在另一个 Spring Bean 中开启事务。本次把业务部分迁移到 Workflow，继续保留独立 Bean 调用关系：

```text
Service 缓存入口 → Workflow 事务开始 → Repository 写入
               ← Workflow 事务提交 ←
成功返回后再执行原有缓存清理
```

这样不会因为把所有方法合并到同一个类而引入自调用绕过代理，或把缓存清理与事务提交的顺序改变。现有 Service 中原本负责的播放历史事务也保留在原位置。

## 如何写数据访问

`BaseRepository<T>` 提供通用记录操作：`getById`、`save`、`updateById`、`insert`、`deleteById`、`deleteBatchIds`。

- `save` / `updateById` 返回是否成功，延续原有 MyBatis-Plus 调用习惯。
- `insert` / `deleteById` / `deleteBatchIds` 返回影响行数，保留原有 Mapper 调用的结果语义。
- 实现内部仍复用项目现有 MyBatis-Plus 3.5.5 的 `ServiceImpl`；不升级依赖。
- 对外接口不暴露 `lambdaQuery()`、`getBaseMapper()` 或任意 Wrapper；业务代码依赖 Repository 接口。
- 新条件查询先在具体 Repository 中声明具名方法，再在实现里写 Wrapper 或调用自定义 Mapper。

歌曲搜索的实际路径：

```text
SongsServiceImpl.getSongsPage
  → SongsWorkflow.getSongsPage        处理页码、组装 GetSongsVO
  → SongsRepository.findPage          数据访问接口
  → SongsRepositoryImpl.findPage      构造过滤、字段选择和排序
  → SongsMapper.selectPage            执行分页 SQL
```

“添加歌曲到歌单”的实际路径：

```text
PlaylistsServiceImpl.addSongToPlaylist  保留原有缓存清理
  → PlaylistsWorkflow.addSongToPlaylist
      校验歌单所有者和歌曲是否存在
      计算下一位置
      调用关系 Repository 写入
      调用歌单 Repository 更新歌曲数量
      组装返回对象
```

## 有 Repository 后，XML 还需要吗？

需要保留现有自定义 SQL。Repository 与 XML 解决不同的问题：Repository 是 Java 数据访问边界；XML 负责描述 SQL 和结果映射。

- 简单单表查询：Repository 实现写 Wrapper，调用 BaseMapper，通常无需 XML。
- 联表、聚合、批量位置调整、动态 SQL：Repository 调用自定义 Mapper 方法，SQL 可以写在 XML。
- 简短自定义 SQL：现有 Mapper 注解仍可使用。
- 嵌套返回结构：继续用 XML `resultMap` / `association` / `collection`。

现有播放历史就是完整例子：

```text
SongsServiceImpl.getPlayHistory
  → PlayHistoryRepository.selectHistoryWithSong
  → PlayHistoryMapper.selectHistoryWithSong
  → modules/song/xml/PlayHistoryMapper.xml
```

队列的联表查询和位置更新同样保留在 `QueueCustomMapper.xml`，由 `QueueCustomRepository` 封装调用。XML 的 `namespace` 始终指向 Mapper 接口，不改成 Repository。

## 验证

在 `backend` 目录运行 `mvn clean test`（或 Windows 下 `mvnw.cmd clean test`）。MySQL 迁移测试需要 Docker。

原有业务测试迁移到 `service/workflow`，保留业务断言。`PersistenceLayerIntegrationTest` 使用独立内存数据库和本地缓存，验证真实调用链中的分页过滤、歌词缓存失效、失败时多表事务回滚，以及 XML 联表和嵌套映射。
