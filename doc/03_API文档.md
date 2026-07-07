# 03 API 接口文档

> **Base URL**：`/api`　**鉴权方式**：JWT Bearer Token
> **文档版本**：V1.0　**编写人**：王茂吉
> 接口总数：96 个，分 5 大模块（认证 / 系统管理 / 基础数据 / 业务功能 / 数据统计）

---

## 一、通用约定

### 1.1 基础信息
| 项 | 值 |
|----|----|
| Base URL | `/api` |
| 协议 | HTTP / HTTPS |
| 请求/响应编码 | UTF-8 |
| 数据格式 | `application/json`（文件上传 `multipart/form-data`） |
| 时间格式 | `yyyy-MM-dd HH:mm:ss`（日期 `yyyy-MM-dd`） |

### 1.2 鉴权方式
除白名单外，**所有接口必须携带请求头**：
```
Authorization: Bearer <token>
```
`<token>` 为登录接口（`POST /api/auth/login`）返回的 JWT（HS256，24h 有效期，payload 含 userId、username、roles、iat、exp）。

**鉴权失败响应**：
```json
{ "code": 401, "message": "未登录或登录已失效，请重新登录", "data": null }
```

**白名单接口**（无需鉴权）：`POST /api/auth/login`、`POST /api/auth/refresh`

**数据权限**：非超管/经理只能看本人 communityId 下数据；业主只能看与本人关联的房屋/账单/工单。

### 1.3 统一响应结构 Result\<T\>
```json
{ "code": 200, "message": "操作成功", "data": { /* 业务数据 */ } }
```
| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，200 成功，其它失败 |
| message | string | 提示信息 |
| data | any | 业务数据（对象/数组/null） |

### 1.4 分页约定
**请求参数**（Query）：`page`（int，默认 1）/ `size`（int，默认 10，最大 100）

**分页响应 PageResult\<T\>**：
| 字段 | 类型 | 说明 |
|------|------|------|
| page | int | 当前页 |
| size | int | 每页条数 |
| total | long | 总记录数 |
| pages | int | 总页数 |
| list | array | 当前页数据列表 |

### 1.5 错误码
**HTTP 级**：
| code | 含义 | 触发场景 |
|------|------|---------|
| 200 | 成功 | 正常业务 |
| 400 | 参数错误 | 参数校验失败 / 格式错 |
| 401 | 未认证 | 未登录 / token 失效 |
| 403 | 无权限 | 已登录但无该接口权限 |
| 404 | 资源不存在 | 查不到记录 |
| 409 | 业务冲突 | 唯一约束冲突、状态不允许操作 |
| 500 | 服务器错误 | 未捕获异常 |

**业务细分码**（2xxx 段）：
| code | 含义 |
|------|------|
| 2001 | 用户名或密码错误 |
| 2002 | 账号已禁用 |
| 2003 | 旧密码错误 |
| 2004 | 用户名已存在 |
| 2101 | 工单状态流转非法 |
| 2102 | 工单已被接单 |
| 2201 | 账单已缴清 |
| 2202 | 缴费金额超过应收 |
| 2203 | 账单已作废 |
| 2301 | 车位已被占用 |
| 2401 | 设备编号已存在 |
| 9999 | 未知系统错误 |

### 1.6 RESTful 规范
```
GET    /api/{module}           # 列表查询（分页）
GET    /api/{module}/{id}      # 详情查询
POST   /api/{module}           # 新增
PUT    /api/{module}/{id}      # 修改
DELETE /api/{module}/{id}      # 删除（逻辑删除）
```
- 路径命名：全小写连字符，资源名词复数；动作 URL 用 `/{id}/{action}`；子资源 `/{id}/{sub}`
- 权限码命名：`模块:子模块:动作`，全小写（如 `system:user:list`）
- 文件上传：`POST /api/common/upload`，multipart/form-data，字段名 `file`，返回 `{ "url": "http://.../xxx.jpg" }`
- 删除约定：逻辑删除（`deleted=1`），并解除相关绑定
- 身份证脱敏：列表/详情默认保留前 6 后 4，中间 `*`

---

## 二、认证模块（`/api/auth`）

| # | 方法 | 路径 | 鉴权 | 权限码 | 说明 | 关键参数 | 响应 data |
|:-:|------|------|:----:|-------|------|---------|-----------|
| 1 | POST | `/login` | 否 | — | 登录 | username*(必)*, password*(必)* | token, expiresAt, userInfo{id,username,realName,userType,roles[],permissions[]} |
| 2 | POST | `/logout` | 是 | — | 注销 | 无 | null |
| 3 | GET | `/info` | 是 | — | 当前用户信息+权限码+菜单树 | 无 | id,username,realName,roles[],permissions[],menus[] |
| 4 | POST | `/refresh` | 否* | — | 刷新 Token（凭旧 token） | 无 | token, expiresAt |
| 5 | PUT | `/password` | 是 | — | 修改密码 | oldPassword*(必)*, newPassword*(必,6-20)* | null |

---

## 三、系统管理模块（`/api/system`，全部需鉴权）

### 3.1 用户管理 `/users`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 1 | GET | `/users` | system:user:list | 分页查询 | username,realName,phone,userType,communityId,status |
| 2 | GET | `/users/{id}` | system:user:query | 用户详情 | id |
| 3 | POST | `/users` | system:user:add | 新增用户 | username*(必,3-20)*, password*(必,6-20)*, realName*(必)*, phone, userType, communityId, roleIds[] |
| 4 | PUT | `/users/{id}` | system:user:edit | 修改用户 | realName*(必)*, phone, userType, communityId, status |
| 5 | DELETE | `/users/{id}` | system:user:delete | 删除（超管不可删） | id |
| 6 | PUT | `/users/{id}/reset-password` | system:user:reset | 重置密码 | newPassword*(必)* |
| 7 | PUT | `/users/{id}/status` | system:user:edit | 启用/禁用 | status*(必,1启用0禁用)* |
| 8 | GET | `/users/{id}/roles` | system:user:query | 查询用户角色 | id |
| 9 | PUT | `/users/{id}/roles` | system:user:assign | 分配角色（全量覆盖） | roleIds*(必,long[])* |

### 3.2 角色管理 `/roles`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 10 | GET | `/roles` | system:role:list | 分页/全量查询 | roleName,roleCode,status（不传 page 返全量，下拉用） |
| 11 | GET | `/roles/{id}` | system:role:query | 角色详情 | id |
| 12 | POST | `/roles` | system:role:add | 新增角色 | roleName*(必)*, roleCode*(必,唯一)*, description, status |
| 13 | PUT | `/roles/{id}` | system:role:edit | 修改（roleCode 不可改） | roleName, description, status |
| 14 | DELETE | `/roles/{id}` | system:role:delete | 删除（需角色下无用户） | id |
| 15 | GET | `/roles/{id}/permissions` | system:role:query | 查询角色权限 | id |
| 16 | PUT | `/roles/{id}/permissions` | system:role:assign | 分配权限（全量覆盖） | permissionIds*(必,long[])* |

### 3.3 权限管理 `/permissions`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 17 | GET | `/permissions/tree` | system:perm:list | 权限树 | 无 |
| 18 | GET | `/permissions` | system:perm:list | 分页查询（平铺） | permName,type,permCode |
| 19 | POST | `/permissions` | system:perm:add | 新增权限 | parentId*(必,0为根)*, permName*(必)*, permCode*(必,唯一)*, type*(必,1菜单2按钮)*, path, icon, sort |
| 20 | PUT | `/permissions/{id}` | system:perm:edit | 修改权限 | 同新增 |
| 21 | DELETE | `/permissions/{id}` | system:perm:delete | 删除（需无子节点） | id |

---

## 四、基础数据模块（`/api/basedata`，全部需鉴权）

### 4.1 小区管理 `/communities`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 1 | GET | `/communities` | basedata:community:list | 分页查询 | name,address,status |
| 2 | GET | `/communities/all` | basedata:community:list | 全部小区（下拉） | 无 |
| 3 | GET | `/communities/{id}` | basedata:community:query | 小区详情 | id |
| 4 | POST | `/communities` | basedata:community:add | 新增小区 | name*(必,唯一)*, address, area, greenRate, buildYear, developer, contactName, contactPhone |
| 5 | PUT | `/communities/{id}` | basedata:community:edit | 修改小区 | 同新增 |
| 6 | DELETE | `/communities/{id}` | basedata:community:delete | 删除（需无楼栋） | id |

### 4.2 楼栋管理 `/buildings`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 7 | GET | `/buildings` | basedata:building:list | 分页查询 | communityId,name,buildingNo |
| 8 | GET | `/buildings/{id}` | basedata:building:query | 楼栋详情 | id |
| 9 | POST | `/buildings` | basedata:building:add | 新增楼栋 | communityId*(必)*, name*(必)*, buildingNo*(必,小区内唯一)*, floors, units, elevators, structureType |
| 10 | PUT | `/buildings/{id}` | basedata:building:edit | 修改楼栋 | 同新增 |
| 11 | DELETE | `/buildings/{id}` | basedata:building:delete | 删除（需无房屋） | id |

### 4.3 房屋管理 `/houses`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 12 | GET | `/houses` | basedata:house:list | 分页查询 | communityId,buildingId,houseNo,status,hasOwner |
| 13 | GET | `/houses/{id}` | basedata:house:query | 房屋详情 | id |
| 14 | POST | `/houses` | basedata:house:add | 新增房屋 | communityId*(必)*, buildingId*(必)*, houseNo*(必,楼栋内唯一)*, unitNo, floorNo, area, layout, status |
| 15 | PUT | `/houses/{id}` | basedata:house:edit | 修改房屋 | 同新增 |
| 16 | DELETE | `/houses/{id}` | basedata:house:delete | 删除 | id |
| 17 | GET | `/houses/{id}/owners` | basedata:house:query | 房屋下业主列表 | id |

### 4.4 业主管理 `/owners`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 18 | GET | `/owners` | basedata:owner:list | 分页查询 | name,phone,idCard,plateNo,status |
| 19 | GET | `/owners/{id}` | basedata:owner:query | 业主详情（含房屋） | id |
| 20 | POST | `/owners` | basedata:owner:add | 新增业主（可同时绑房屋） | name*(必)*, phone*(必)*, idCard*(唯一)*, gender, plateNo, moveInDate, status, houseBindings[{houseId,relation,isPrimary}] |
| 21 | PUT | `/owners/{id}` | basedata:owner:edit | 修改业主 | name,phone,idCard,gender,plateNo,moveInDate,status |
| 22 | DELETE | `/owners/{id}` | basedata:owner:delete | 删除（解除房屋绑定） | id |
| 23 | GET | `/owners/{id}/houses` | basedata:owner:query | 业主名下房屋 | id |

---

## 五、业务功能模块（`/api/business`，全部需鉴权）

### 5.1 工单管理 `/workorders`
工单状态机：1待派单→2已派单→3处理中→4已完成→5已关闭；①②③→6已撤销。

| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 1 | GET | `/workorders` | business:workorder:list | 分页查询 | communityId,orderNo,houseId,ownerId,type,priority,status,handlerId,startDate,endDate |
| 2 | GET | `/workorders/{id}` | business:workorder:query | 工单详情（含日志） | id |
| 3 | POST | `/workorders` | business:workorder:add | 新增工单（orderNo 后端生成 WO+yyyyMMdd+3位流水） | communityId*(必)*, houseId, ownerId, title*(必)*, type*(必)*, priority, description, images[] |
| 4 | PUT | `/workorders/{id}` | business:workorder:edit | 修改工单（仅待派单） | title,type,priority,description,images[] |
| 5 | POST | `/workorders/{id}/assign` | business:workorder:assign | 派单（①→②） | handlerId*(必)*, remark |
| 6 | POST | `/workorders/{id}/accept` | business:workorder:accept | 接单（②→③） | remark |
| 7 | POST | `/workorders/{id}/finish` | business:workorder:finish | 处理完成（③→④） | handleResult*(必)*, images[] |
| 8 | POST | `/workorders/{id}/close` | business:workorder:close | 关闭（④→⑤） | remark |
| 9 | POST | `/workorders/{id}/cancel` | business:workorder:cancel | 撤销（①②③→⑥） | reason*(必)* |
| 10 | POST | `/workorders/{id}/rate` | business:workorder:rate | 评价（1-5 分） | rating*(必,1-5)*, ratingComment |

### 5.2 费用管理
**收费项目 `/fee-items`**：
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 11 | GET | `/fee-items` | business:fee:list | 收费项目列表 | name,type,status |
| 12 | POST | `/fee-items` | business:fee:add | 新增收费项目 | name,type,unit,unitPrice,billingCycle,status |
| 13 | PUT | `/fee-items/{id}` | business:fee:edit | 修改收费项目 | 同新增 |
| 14 | DELETE | `/fee-items/{id}` | business:fee:delete | 删除收费项目 | id |

**账单 `/bills`**：
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 15 | GET | `/bills` | business:bill:list | 分页查询账单 | communityId,houseId,ownerId,feeItemType,status,period,overdue |
| 16 | GET | `/bills/{id}` | business:bill:query | 账单详情（含缴费流水） | id |
| 17 | POST | `/bills/generate` | business:bill:generate | 批量生成账单（金额=单价×数量） | communityId*(必)*, buildingId, feeItemId*(必)*, period*(必,yyyy-MM)*, dueDate |
| 18 | POST | `/bills/{id}/pay` | business:bill:pay | 缴费（可部分缴费） | amount*(必)*, payMethod*(必,1-5)*, remark |
| 19 | POST | `/bills/{id}/void` | business:bill:void | 作废账单（仅未缴状态） | reason*(必)* |
| 20 | GET | `/bills/export` | business:bill:export | 导出 Excel | 同 #15 Query，返回 .xlsx 二进制 |

### 5.3 车位管理 `/parking`
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 21 | GET | `/parking/spaces` | business:parking:list | 分页查询车位 | communityId,spaceNo,areaType,useType,status,ownerId,plateNo |
| 22 | GET | `/parking/spaces/{id}` | business:parking:query | 车位详情 | id |
| 23 | POST | `/parking/spaces` | business:parking:add | 新增车位 | communityId*(必)*, spaceNo*(必,小区内唯一)*, areaType*(必)*, useType*(必)*, monthlyFee, status |
| 24 | PUT | `/parking/spaces/{id}` | business:parking:edit | 修改车位 | 同新增 |
| 25 | DELETE | `/parking/spaces/{id}` | business:parking:delete | 删除车位 | id |
| 26 | POST | `/parking/spaces/{id}/bind` | business:parking:bind | 绑定业主/车牌 | ownerId*(必)*, plateNo, startDate, endDate, amount |
| 27 | POST | `/parking/spaces/{id}/unbind` | business:parking:unbind | 解绑 | remark |
| 28 | POST | `/parking/spaces/{id}/renew` | business:parking:renew | 长租续费 | months*(必)*, amount, remark |
| 29 | GET | `/parking/records` | business:parking:list | 车位操作记录 | spaceId,ownerId,action |

### 5.4 设备管理 `/equipments`（通闸/消防/安防统一，category 区分：1通闸 2消防 3安防）
| # | 方法 | 路径 | 权限码 | 说明 | 关键参数 |
|:-:|------|------|-------|------|---------|
| 30 | GET | `/equipments` | business:equipment:list | 分页查询设备 | communityId,category,name,code,status,onlineStatus |
| 31 | GET | `/equipments/{id}` | business:equipment:query | 设备详情 | id |
| 32 | POST | `/equipments` | business:equipment:add | 新增设备 | communityId*(必)*, category*(必)*, name*(必)*, code*(必,小区+类别内唯一)*, location, model, manufacturer, installDate, warrantyDate, onlineStatus, status, nextCheckDate |
| 33 | PUT | `/equipments/{id}` | business:equipment:edit | 修改设备 | 同新增 |
| 34 | DELETE | `/equipments/{id}` | business:equipment:delete | 删除设备 | id |
| 35 | POST | `/equipments/{id}/check` | business:equipment:check | 提交巡检（异常自动置故障） | result*(必,1正常2异常)*, issueDesc*(result=2必填)*, images[], nextCheckDate |
| 36 | GET | `/equipments/{id}/checks` | business:equipment:query | 巡检记录 | page,size |
| 37 | GET | `/equipments/expiring` | business:equipment:list | 到期/待检提醒（保修30天内/巡检≤7天/离线） | communityId, type(warranty/check/offline) |

---

## 六、数据统计模块（`/api/stat`，全部需鉴权）

通用 Query：`communityId`（否，不传按当前用户所属小区，超管可查全部）、`startDate`/`endDate`（否，yyyy-MM-dd）。

| # | 方法 | 路径 | 权限码 | 说明 | 响应 data 核心字段 |
|:-:|------|------|-------|------|----------------|
| 1 | GET | `/dashboard` | stat:dashboard:view | 仪表盘概览 | houseTotal,ownerTotal,checkInRate,monthReceivable,monthReceived,collectionRate,workorderPending,equipmentOnlineRate,todoList[]{type,count,desc} |
| 2 | GET | `/charge/summary` | stat:charge:view | 收费汇总 | receivable,received,unpaid,collectionRate,billCount,paidCount,overdueCount |
| 3 | GET | `/charge/trend` | stat:charge:view | 收费趋势（折线图） | {categories[],receivable[],received[]} |
| 4 | GET | `/charge/by-type` | stat:charge:view | 按费用类型占比（饼图） | [{name,value,percent}] |
| 5 | GET | `/workorder/summary` | stat:workorder:view | 工单汇总 | total,finished,handling,pending,canceled,avgHandleHours,avgRating |
| 6 | GET | `/workorder/by-status` | stat:workorder:view | 工单状态分布 | [{status,name,count}] |
| 7 | GET | `/workorder/by-handler` | stat:workorder:view | 维修员工单量排名 | [{handlerId,handlerName,finishedCount,avgHandleHours,avgRating}] |
| 8 | GET | `/parking/usage` | stat:parking:view | 车位使用率 | total,inUse,free,sold,usageRate,byArea[]{areaType,total,inUse,usageRate} |
| 9 | GET | `/equipment/status` | stat:equipment:view | 设备状态统计（按类别） | [{category,total,normal,fault,repairing,scrapped,onlineRate}] |
| 10 | GET | `/export` | stat:export | 综合报表导出 Excel（多 sheet） | type*(必,charge/workorder/parking/equipment)*，返回 .xlsx 二进制 |

---

## 七、请求示例

### 7.1 登录
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```
响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresAt": 1752004800000,
    "userInfo": { "id": 1, "username": "admin", "realName": "超级管理员", "userType": 1, "roles": ["SUPER_ADMIN"], "permissions": ["*"] }
  }
}
```

### 7.2 分页查询用户（带 Token）
```http
GET /api/system/users?page=1&size=10&username=张
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 7.3 工单派单
```http
POST /api/business/workorders/1/assign
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{ "handlerId": 4, "remark": "指派赵维修处理" }
```

### 7.4 账单缴费
```http
POST /api/business/bills/1/pay
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{ "amount": 500.00, "payMethod": 2, "remark": "微信支付" }
```

---

**文档编写**：王茂吉　　**审核**：杨和宇　　**日期**：2026-07-05
