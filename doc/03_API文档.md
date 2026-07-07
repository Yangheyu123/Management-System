# 03 API 接口文档（入口）

> **Base URL**：`/api`
> **鉴权方式**：JWT Bearer Token（请求头 `Authorization: Bearer <token>`）
> **统一响应格式**：见 [`04-接口规范文档.md`](./04-接口规范文档.md)

本系统接口按模块拆分为 5 份详细文档，请按需查阅：

| 模块 | 文档 | 接口前缀 | 接口数 |
|------|------|---------|:-----:|
| 用户认证 | [API-01-认证模块接口文档.md](./API-01-认证模块接口文档.md) | `/api/auth` | 5 |
| 系统管理 | [API-02-系统管理接口文档.md](./API-02-系统管理接口文档.md) | `/api/system` | ~15 |
| 基础数据 | [API-03-基础数据接口文档.md](./API-03-基础数据接口文档.md) | `/api/basedata` | ~20 |
| 业务功能 | [API-04-业务功能接口文档.md](./API-04-业务功能接口文档.md) | `/api/business` | ~30 |
| 数据统计 | [API-05-数据统计接口文档.md](./API-05-数据统计接口文档.md) | `/api/stat` | 9 |

---

## 一、统一约定

### 1.1 统一响应格式

所有接口统一返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200 成功，其他失败 |
| message | string | 提示信息 |
| data | object/array | 业务数据 |

### 1.2 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pages": 10,
    "current": 1,
    "size": 10,
    "records": []
  }
}
```

### 1.3 错误码说明

| code | 含义 | 触发场景 |
|------|------|---------|
| 200 | 成功 | 正常请求 |
| 400 | 参数错误 | 参数校验失败 |
| 401 | 未认证 | Token 缺失或失效 |
| 403 | 无权限 | 权限校验不通过 |
| 404 | 资源不存在 | 查询对象不存在 |
| 500 | 服务器错误 | 业务异常或系统异常 |

### 1.4 通用分页参数

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| current | int | 1 | 当前页码 |
| size | int | 10 | 每页条数 |

### 1.5 RESTful 规范

```
GET    /api/{module}           # 列表查询（分页）
GET    /api/{module}/{id}      # 详情查询
POST   /api/{module}           # 新增
PUT    /api/{module}/{id}      # 修改
DELETE /api/{module}/{id}      # 删除
```

---

## 二、各模块接口速览

### 2.1 认证模块（`/api/auth`）
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|:----:|
| POST | `/login` | 登录 | 否 |
| POST | `/logout` | 注销 | 是 |
| GET | `/info` | 当前用户信息 | 是 |
| POST | `/refresh` | 刷新 Token | 是 |
| PUT | `/password` | 修改密码 | 是 |

### 2.2 系统管理（`/api/system`）
- **用户** `/users`：列表/详情/新增/修改/删除/重置密码/启停/角色查询/角色分配
- **角色** `/roles`：列表/详情/新增/修改/删除/权限查询/权限分配
- **权限** `/permissions`：权限树/列表/新增/修改/删除

### 2.3 基础数据（`/api/basedata`）
- **小区** `/communities`：列表/全部/详情/新增/修改/删除
- **楼栋** `/buildings`：列表/详情/新增/修改/删除
- **房屋** `/houses`：列表/详情/新增/修改/删除/关联业主查询
- **业主** `/owners`：列表/详情/新增/修改/删除/关联房屋查询

### 2.4 业务功能（`/api/business`）
- **工单** `/workorders`：列表/详情/新增/修改/派单/接单/完工/关闭/撤销/评价
- **设备** `/equipments`：列表/到期预警/详情/新增/修改/删除/巡检登记/巡检记录
- **车位** `/parking`：车位CRUD/绑定/解绑/续费 + 进出记录
- **收费项目** `/fee-items`：列表/新增/修改/删除
- **账单** `/bills`：列表/详情/批量生成/缴费/作废/导出

### 2.5 数据统计（`/api/stat`）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard` | 综合看板 |
| GET | `/charge/summary` | 收费汇总 |
| GET | `/charge/trend` | 收费趋势 |
| GET | `/charge/by-type` | 按类型收费 |
| GET | `/workorder/summary` | 工单汇总 |
| GET | `/workorder/by-status` | 工单按状态 |
| GET | `/workorder/by-handler` | 工单按处理人 |
| GET | `/parking/usage` | 车位使用率 |
| GET | `/equipment/status` | 设备状态 |

---

> **详细的请求参数、响应示例、字段说明**请查阅对应模块的 API 文档。

**文档编写**：王茂吉　　**审核**：杨和宇　　**日期**：2026-07-05
