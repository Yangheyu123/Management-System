# API-01 - 认证模块接口文档

> 模块：用户认证（登录 / 注销 / Token 全局鉴权）
> Base URL：`/api` ｜ 鉴权约定见 [04 接口规范文档](./04-接口规范文档.md)

## 接口清单

| # | 方法 | 路径 | 鉴权 | 说明 |
| -- | ---- | ---- | ---- | ---- |
| 1 | POST | /auth/login | 否 | 登录，返回 JWT |
| 2 | POST | /auth/logout | 是 | 注销 |
| 3 | GET  | /auth/info | 是 | 获取当前登录用户 + 权限码 |
| 4 | POST | /auth/refresh | 否* | 刷新 token（凭旧 token） |
| 5 | PUT  | /auth/password | 是 | 修改自己的密码 |

> *refresh 不走标准鉴权拦截器，但需校验传入 token 是否合法且未过期。

---

## 1. 登录

`POST /api/auth/login`

**简介**：账号密码登录，校验通过后生成 JWT 返回。密码用 BCrypt 比对。

**请求体**（`application/json`）：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| username | string | 是 | 登录账号 |
| password | string | 是 | 明文密码（前端可先做一次 SHA，但建议 HTTPS 直传） |

**请求示例**：

```json
{ "username": "admin", "password": "admin123" }
```

**响应体** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| token | string | JWT，后续请求放 Authorization 头 |
| expiresAt | string | 过期时间 `yyyy-MM-dd HH:mm:ss` |
| userInfo | object | 用户基本信息 |
| userInfo.id | long | 用户 id |
| userInfo.username | string | 账号 |
| userInfo.realName | string | 姓名 |
| userInfo.avatar | string | 头像 URL |
| userInfo.userType | int | 1员工 2业主 |
| userInfo.communityId | long | 所属小区 |
| userInfo.roles | string[] | 角色编码数组，如 `["SUPER_ADMIN"]` |
| userInfo.permissions | string[] | 权限码数组，如 `["system:user:add"]` |

**响应示例**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTczMzA5NzYwMH0.xxx",
    "expiresAt": "2026-07-04 10:00:00",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "超级管理员",
      "avatar": null,
      "userType": 1,
      "communityId": null,
      "roles": ["SUPER_ADMIN"],
      "permissions": ["*"]
    }
  }
}
```

**错误码**：

| code | message |
| ---- | ---- |
| 2001 | 用户名或密码错误 |
| 2002 | 账号已被禁用，请联系管理员 |
| 400 | 用户名/密码不能为空 |

---

## 2. 注销

`POST /api/auth/logout`

**简介**：注销当前登录态。后端将当前 token 加入黑名单（内存或 Redis，可选），前端清除本地 token。

**权限**：已登录（任意角色）

**请求头**：需携带 `Authorization: Bearer <token>`

**请求体**：无

**响应示例**：

```json
{ "code": 200, "message": "已注销", "data": null }
```

**错误码**：401（未登录）

---

## 3. 获取当前登录用户信息

`GET /api/auth/info`

**简介**：前端刷新页面时调用，恢复登录态。返回当前用户信息 + 完整权限码（用于动态路由和按钮控制）。

**权限**：已登录

**响应体** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | 用户 id |
| username | string | 账号 |
| realName | string | 姓名 |
| phone | string | 手机号 |
| email | string | 邮箱 |
| avatar | string | 头像 |
| gender | int | 性别 |
| userType | int | 1员工 2业主 |
| communityId | long | 所属小区 |
| communityName | string | 所属小区名 |
| roles | string[] | 角色编码 |
| roleNames | string[] | 角色名 |
| permissions | string[] | 权限码（超管为 `["*"]`） |
| menus | array | 前端可见的菜单树（按权限过滤后） |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2,
    "username": "manager01",
    "realName": "张经理",
    "phone": "13800000002",
    "email": "manager@xx.com",
    "avatar": null,
    "gender": 1,
    "userType": 1,
    "communityId": 1,
    "communityName": "阳光花园小区",
    "roles": ["PROPERTY_MANAGER"],
    "roleNames": ["物业经理"],
    "permissions": [
      "basedata:community:list", "basedata:building:list",
      "business:workorder:list", "business:bill:list", "stat:dashboard:view"
    ],
    "menus": [
      { "id": 10, "name": "仪表盘", "path": "/dashboard", "icon": "Odometer" },
      { "id": 20, "name": "基础数据", "path": "/basedata", "icon": "Folder",
        "children": [
          { "id": 21, "name": "小区管理", "path": "/basedata/community" },
          { "id": 22, "name": "楼栋管理", "path": "/basedata/building" }
        ]
      }
    ]
  }
}
```

**错误码**：401（未登录/token 失效）

---

## 4. 刷新 Token

`POST /api/auth/refresh`

**简介**：在 token 即将过期前，前端用旧 token 换新 token，避免用户重新登录。

**请求头**：`Authorization: Bearer <旧token>`

**请求体**：无

**响应体** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| token | string | 新 JWT |
| expiresAt | string | 新过期时间 |

**响应示例**：

```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...(new)",
    "expiresAt": "2026-07-04 11:00:00"
  }
}
```

**错误码**：

| code | message |
| ---- | ---- |
| 401 | token 已失效，请重新登录 |

---

## 5. 修改自己的密码

`PUT /api/auth/password`

**简介**：当前登录用户修改自己的登录密码。需提供旧密码校验。

**权限**：已登录

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| oldPassword | string | 是 | 旧密码（明文） |
| newPassword | string | 是 | 新密码（明文，长度 6-20） |

**请求示例**：

```json
{ "oldPassword": "admin123", "newPassword": "admin@2026" }
```

**响应示例**：

```json
{ "code": 200, "message": "密码修改成功，请重新登录", "data": null }
```

**错误码**：

| code | message |
| ---- | ---- |
| 2003 | 旧密码错误 |
| 400 | 新密码长度需在 6-20 位之间 |

---

## 附：JWT 结构（参考）

Header：
```json
{ "alg": "HS256", "typ": "JWT" }
```

Payload：
```json
{
  "sub": "1",                 // userId
  "username": "admin",
  "realName": "超级管理员",
  "roles": ["SUPER_ADMIN"],
  "iat": 1733011200,
  "exp": 1733097600           // 24h
}
```

签名：`HMACSHA256(base64(header) + "." + base64(payload), secret)`，secret 配置在 `application.yml` 的 `jwt.secret`。

## 相关文档

- [04 - 接口规范文档](./04-接口规范文档.md)
- [API-02 - 系统管理接口文档](./API-02-系统管理接口文档.md)
