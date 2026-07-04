# API-02 - 系统管理接口文档（用户 / 角色 / 权限）

> 模块：系统管理（RBAC：用户管理、角色管理、权限管理）
> Base URL：`/api` ｜ 全部接口需登录鉴权 ｜ 通用约定见 [04 接口规范文档](./04-接口规范文档.md)

## 接口清单

**用户管理**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 1 | GET | /system/users | system:user:list |
| 2 | GET | /system/users/{id} | system:user:query |
| 3 | POST | /system/users | system:user:add |
| 4 | PUT | /system/users/{id} | system:user:edit |
| 5 | DELETE | /system/users/{id} | system:user:delete |
| 6 | PUT | /system/users/{id}/reset-password | system:user:reset |
| 7 | PUT | /system/users/{id}/status | system:user:edit |
| 8 | GET | /system/users/{id}/roles | system:user:query |
| 9 | PUT | /system/users/{id}/roles | system:user:assign |

**角色管理**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 10 | GET | /system/roles | system:role:list |
| 11 | GET | /system/roles/{id} | system:role:query |
| 12 | POST | /system/roles | system:role:add |
| 13 | PUT | /system/roles/{id} | system:role:edit |
| 14 | DELETE | /system/roles/{id} | system:role:delete |
| 15 | GET | /system/roles/{id}/permissions | system:role:query |
| 16 | PUT | /system/roles/{id}/permissions | system:role:assign |

**权限管理**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 17 | GET | /system/permissions/tree | system:perm:list |
| 18 | GET | /system/permissions | system:perm:list |
| 19 | POST | /system/permissions | system:perm:add |
| 20 | PUT | /system/permissions/{id} | system:perm:edit |
| 21 | DELETE | /system/permissions/{id} | system:perm:delete |

---

# 一、用户管理

## 1. 分页查询用户列表

`GET /api/system/users`

**简介**：支持按用户名、姓名、手机号、状态、所属小区筛选；返回分页结果。

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| username | string | 否 | 账号，模糊匹配 |
| realName | string | 否 | 姓名，模糊 |
| phone | string | 否 | 手机号，模糊 |
| userType | int | 否 | 1员工 2业主 |
| communityId | long | 否 | 所属小区 |
| status | int | 否 | 1启用 0禁用 |

**请求示例**：

```
GET /api/system/users?page=1&size=10&realName=张&status=1
```

**响应** `data`（PageResult<UserVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| page/size/total/pages | | 分页元信息 |
| list[].id | long | 用户 id |
| list[].username | string | 账号 |
| list[].realName | string | 姓名 |
| list[].phone | string | 手机 |
| list[].email | string | 邮箱 |
| list[].gender | int | 性别 |
| list[].userType | int | 用户类型 |
| list[].communityId | long | 小区 |
| list[].communityName | string | 小区名 |
| list[].status | int | 状态 |
| list[].roleNames | string[] | 角色名 |
| list[].lastLoginTime | string | 最后登录 |
| list[].createTime | string | 创建时间 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "page": 1, "size": 10, "total": 25, "pages": 3,
    "list": [
      {
        "id": 2,
        "username": "manager01",
        "realName": "张经理",
        "phone": "13800000002",
        "email": "manager@xx.com",
        "gender": 1,
        "userType": 1,
        "communityId": 1,
        "communityName": "阳光花园小区",
        "status": 1,
        "roleNames": ["物业经理"],
        "lastLoginTime": "2026-07-02 18:30:11",
        "createTime": "2026-06-01 09:00:00"
      }
    ]
  }
}
```

**错误码**：401（未登录）/ 403（无权限）

---

## 2. 用户详情

`GET /api/system/users/{id}`

**Path 参数**：`id` (long) 用户 id

**响应** `data`（UserVO，含角色 id 数组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, username, realName, phone, email, gender, avatar | | 基本信息 |
| userType | int | 用户类型 |
| communityId / communityName | | 小区 |
| status | int | 状态 |
| roleIds | long[] | 角色 id 列表 |
| roleNames | string[] | 角色名 |
| lastLoginTime, createTime, updateTime | | 时间 |

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
    "gender": 1,
    "avatar": null,
    "userType": 1,
    "communityId": 1,
    "communityName": "阳光花园小区",
    "status": 1,
    "roleIds": [2],
    "roleNames": ["物业经理"],
    "lastLoginTime": "2026-07-02 18:30:11",
    "createTime": "2026-06-01 09:00:00",
    "updateTime": "2026-07-02 18:30:11"
  }
}
```

**错误码**：404（用户不存在）

---

## 3. 新增用户

`POST /api/system/users`

**简介**：创建账号。密码经 BCrypt 加密后入库。username 唯一。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| username | string | 是 | 账号，唯一，3-20 位 |
| password | string | 是 | 初始密码明文，6-20 位 |
| realName | string | 是 | 姓名 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| gender | int | 否 | 性别 |
| userType | int | 否 | 默认 1 |
| communityId | long | 否 | 所属小区 |
| roleIds | long[] | 否 | 角色 id 列表 |
| status | int | 否 | 默认 1 |

**请求示例**：

```json
{
  "username": "toll01",
  "password": "123456",
  "realName": "李收费",
  "phone": "13900000001",
  "gender": 2,
  "userType": 1,
  "communityId": 1,
  "roleIds": [3]
}
```

**响应示例**：

```json
{ "code": 200, "message": "新增成功", "data": { "id": 10 } }
```

**错误码**：

| code | message |
| ---- | ---- |
| 2004 | 用户名已存在 |
| 400 | 参数校验失败（账号/密码/姓名为空等） |

---

## 4. 修改用户

`PUT /api/system/users/{id}`

**Path 参数**：`id`

**请求体**（不含 username 和 password，这两个分别由专门的接口处理）：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| realName | string | 是 | 姓名 |
| phone, email, gender | | 否 | |
| userType | int | 否 | |
| communityId | long | 否 | |
| status | int | 否 | |

**响应示例**：

```json
{ "code": 200, "message": "修改成功", "data": null }
```

**错误码**：404（不存在）/ 400

---

## 5. 删除用户

`DELETE /api/system/users/{id}`

**简介**：逻辑删除（`deleted=1`）。超级管理员不可删除。

**Path 参数**：`id`

**响应示例**：

```json
{ "code": 200, "message": "删除成功", "data": null }
```

**错误码**：

| code | message |
| ---- | ---- |
| 404 | 用户不存在 |
| 409 | 超级管理员账号不可删除 |

---

## 6. 重置密码

`PUT /api/system/users/{id}/reset-password`

**简介**：管理员重置某用户密码为指定值（不校验旧密码）。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| newPassword | string | 是 | 新密码，6-20 位 |

**响应示例**：

```json
{ "code": 200, "message": "密码已重置", "data": null }
```

**错误码**：404 / 400

---

## 7. 启用/禁用用户

`PUT /api/system/users/{id}/status`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| status | int | 是 | 1启用 0禁用 |

**响应示例**：

```json
{ "code": 200, "message": "操作成功", "data": null }
```

**错误码**：404 / 409（禁用超管）

---

## 8. 查询用户角色

`GET /api/system/users/{id}/roles`

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| userId | long | |
| roleIds | long[] | 已分配角色 id |
| roles | object[] | 角色详情：{id, roleName, roleCode} |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": { "userId": 2, "roleIds": [2], "roles": [
    { "id": 2, "roleName": "物业经理", "roleCode": "PROPERTY_MANAGER" }
  ]}
}
```

---

## 9. 分配用户角色

`PUT /api/system/users/{id}/roles`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| roleIds | long[] | 是 | 角色 id 列表（全量覆盖） |

**请求示例**：

```json
{ "roleIds": [2, 4] }
```

**响应示例**：

```json
{ "code": 200, "message": "分配成功", "data": null }
```

**错误码**：404 / 400（roleIds 为空数组允许，表示清空角色）

---

# 二、角色管理

## 10. 查询角色列表

`GET /api/system/roles`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page | int | 否 | 不传则返回全量（下拉用） |
| size | int | 否 | |
| roleName | string | 否 | 模糊 |
| roleCode | string | 否 | 模糊 |
| status | int | 否 | |

**响应** `data`：分页则 PageResult，全量则数组 `RoleVO[]`。

RoleVO 字段：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| roleName | string | 角色名 |
| roleCode | string | 编码 |
| description | string | 描述 |
| status | int | |
| userCount | int | 该角色下用户数 |
| createTime | string | |

**响应示例**（全量）：

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    { "id": 1, "roleName": "超级管理员", "roleCode": "SUPER_ADMIN", "description": "拥有全部权限", "status": 1, "userCount": 1, "createTime": "2026-06-01 09:00:00" },
    { "id": 2, "roleName": "物业经理",   "roleCode": "PROPERTY_MANAGER", "status": 1, "userCount": 3, "createTime": "2026-06-01 09:00:00" }
  ]
}
```

---

## 11. 角色详情

`GET /api/system/roles/{id}`

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, roleName, roleCode, description, status | | |
| permissionIds | long[] | 已分配权限 id |
| createTime, updateTime | | |

---

## 12. 新增角色

`POST /api/system/roles`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| roleName | string | 是 | |
| roleCode | string | 是 | 唯一，大写+下划线 |
| description | string | 否 | |
| status | int | 否 | 默认 1 |

**响应示例**：

```json
{ "code": 200, "message": "新增成功", "data": { "id": 6 } }
```

**错误码**：409（roleCode 已存在）/ 400

---

## 13. 修改角色

`PUT /api/system/roles/{id}`

**请求体**：`roleName`、`description`、`status`（roleCode 不可改）。

**错误码**：404 / 409（内置角色不可改名）

---

## 14. 删除角色

`DELETE /api/system/roles/{id}`

**简介**：删除前需校验该角色下无用户，否则提示先解绑。

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 该角色下仍有用户，请先解除关联 |
| 409 | 内置角色不可删除 |

---

## 15. 查询角色权限

`GET /api/system/roles/{id}/permissions`

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| roleId | long | |
| permissionIds | long[] | 已勾选权限 id |

---

## 16. 给角色分配权限

`PUT /api/system/roles/{id}/permissions`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| permissionIds | long[] | 是 | 权限 id 全量覆盖 |

**响应示例**：

```json
{ "code": 200, "message": "分配成功", "data": null }
```

---

# 三、权限管理

## 17. 权限树（菜单）

`GET /api/system/permissions/tree`

**简介**：返回树形结构，供前端菜单渲染和角色分配权限时勾选。

**响应** `data`（TreeNode 数组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| parentId | long | |
| permName | string | |
| permCode | string | |
| type | int | 1菜单 2按钮 |
| path | string | 路由 |
| icon | string | |
| sort | int | |
| children | array | 子节点 |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    {
      "id": 100, "parentId": 0, "permName": "系统管理", "permCode": "system", "type": 1,
      "path": "/system", "icon": "Setting", "sort": 1,
      "children": [
        { "id": 101, "parentId": 100, "permName": "用户管理", "permCode": "system:user", "type": 1, "path": "/system/user", "icon": "User", "sort": 1,
          "children": [
            { "id": 1011, "parentId": 101, "permName": "查询", "permCode": "system:user:list", "type": 2 },
            { "id": 1012, "parentId": 101, "permName": "新增", "permCode": "system:user:add", "type": 2 },
            { "id": 1013, "parentId": 101, "permName": "修改", "permCode": "system:user:edit", "type": 2 },
            { "id": 1014, "parentId": 101, "permName": "删除", "permCode": "system:user:delete", "type": 2 }
          ]
        }
      ]
    }
  ]
}
```

---

## 18. 分页查询权限（平铺）

`GET /api/system/permissions`

**Query 参数**：`page`、`size`、`permName`、`type`、`permCode`

**响应** `data`：PageResult<PermissionVO>，字段同树节点（无 children）。

---

## 19. 新增权限

`POST /api/system/permissions`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| parentId | long | 是 | 0 为根 |
| permName | string | 是 | |
| permCode | string | 是 | 唯一 |
| type | int | 是 | 1菜单 2按钮 |
| path | string | 否 | 菜单路由 |
| icon | string | 否 | |
| sort | int | 否 | |

**错误码**：409（permCode 重复）/ 400

---

## 20. 修改权限

`PUT /api/system/permissions/{id}`

**请求体**：同新增（parentId 不能把自己设为自己的子节点，防环）。

**错误码**：404 / 400（循环引用）

---

## 21. 删除权限

`DELETE /api/system/permissions/{id}`

**简介**：删除菜单/按钮。若有子节点需先删子节点；若已被角色引用会自动解除关联。

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 存在子节点，请先删除子节点 |

---

## 附：权限码命名规范

`模块:子模块:动作`，全小写。

| 示例 | 含义 |
| ---- | ---- |
| `system:user:list` | 用户管理-查询 |
| `system:user:add` | 用户管理-新增 |
| `basedata:community:list` | 基础数据-小区-查询 |
| `business:workorder:assign` | 工单-派单 |
| `business:bill:pay` | 账单-缴费 |
| `stat:dashboard:view` | 仪表盘-查看 |

## 相关文档

- [04 - 接口规范文档](./04-接口规范文档.md)
- [API-01 - 认证模块接口文档](./API-01-认证模块接口文档.md)
- [API-03 - 基础数据接口文档](./API-03-基础数据接口文档.md)
