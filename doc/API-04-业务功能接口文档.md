# API-04 - 业务功能接口文档（工单 / 费用 / 车位 / 设备）

> 模块：业务功能
> Base URL：`/api` ｜ 全部接口需登录鉴权 ｜ 通用约定见 [04 接口规范文档](./04-接口规范文档.md)

## 接口清单

**工单管理**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 1 | GET | /business/workorders | business:workorder:list |
| 2 | GET | /business/workorders/{id} | business:workorder:query |
| 3 | POST | /business/workorders | business:workorder:add |
| 4 | PUT | /business/workorders/{id} | business:workorder:edit |
| 5 | POST | /business/workorders/{id}/assign | business:workorder:assign |
| 6 | POST | /business/workorders/{id}/accept | business:workorder:accept |
| 7 | POST | /business/workorders/{id}/finish | business:workorder:finish |
| 8 | POST | /business/workorders/{id}/close | business:workorder:close |
| 9 | POST | /business/workorders/{id}/cancel | business:workorder:cancel |
| 10 | POST | /business/workorders/{id}/rate | business:workorder:rate |

**费用管理**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 11 | GET | /business/fee-items | business:fee:list |
| 12 | POST | /business/fee-items | business:fee:add |
| 13 | PUT | /business/fee-items/{id} | business:fee:edit |
| 14 | DELETE | /business/fee-items/{id} | business:fee:delete |
| 15 | GET | /business/bills | business:bill:list |
| 16 | GET | /business/bills/{id} | business:bill:query |
| 17 | POST | /business/bills/generate | business:bill:generate |
| 18 | POST | /business/bills/{id}/pay | business:bill:pay |
| 19 | POST | /business/bills/{id}/void | business:bill:void |
| 20 | GET | /business/bills/export | business:bill:export |

**车位管理**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 21 | GET | /business/parking/spaces | business:parking:list |
| 22 | GET | /business/parking/spaces/{id} | business:parking:query |
| 23 | POST | /business/parking/spaces | business:parking:add |
| 24 | PUT | /business/parking/spaces/{id} | business:parking:edit |
| 25 | DELETE | /business/parking/spaces/{id} | business:parking:delete |
| 26 | POST | /business/parking/spaces/{id}/bind | business:parking:bind |
| 27 | POST | /business/parking/spaces/{id}/unbind | business:parking:unbind |
| 28 | POST | /business/parking/spaces/{id}/renew | business:parking:renew |
| 29 | GET | /business/parking/records | business:parking:list |

**设备管理（通闸/消防/安防统一）**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 30 | GET | /business/equipments | business:equipment:list |
| 31 | GET | /business/equipments/{id} | business:equipment:query |
| 32 | POST | /business/equipments | business:equipment:add |
| 33 | PUT | /business/equipments/{id} | business:equipment:edit |
| 34 | DELETE | /business/equipments/{id} | business:equipment:delete |
| 35 | POST | /business/equipments/{id}/check | business:equipment:check |
| 36 | GET | /business/equipments/{id}/checks | business:equipment:query |
| 37 | GET | /business/equipments/expiring | business:equipment:list |

---

# 一、工单管理

## 工单状态机

```
                  assign          accept           finish           close
  ①待派单 ──────▶ ②已派单 ──────▶ ③处理中 ──────▶ ④已完成 ──────▶ ⑤已关闭
     │                │                                              ▲
     │                │ cancel                                       │
     └────────────────┴─────────────────────────────────────▶ ⑥已撤销 ┘
```

| 状态值 | 名称 |
| ---- | ---- |
| 1 | 待派单 |
| 2 | 已派单 |
| 3 | 处理中 |
| 4 | 已完成 |
| 5 | 已关闭 |
| 6 | 已撤销 |

---

## 1. 分页查询工单

`GET /api/business/workorders`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| communityId | long | 否 | |
| orderNo | string | 否 | 工单号 |
| houseId | long | 否 | |
| ownerId | long | 否 | |
| type | int | 否 | 类型 |
| priority | int | 否 | 优先级 |
| status | int | 否 | 状态 |
| handlerId | long | 否 | 维修员 |
| startDate / endDate | string | 否 | 创建时间区间 yyyy-MM-dd |

**响应** `data`（PageResult<WorkOrderVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| orderNo | string | 工单号 |
| communityName | string | 小区 |
| houseNo | string | 房号（可空） |
| ownerName | string | 报修人 |
| ownerPhone | string | 报修电话 |
| title | string | 标题 |
| type | int | 类型 |
| typeName | string | 类型名 |
| priority | int | 优先级 |
| priorityName | string | |
| status | int | 状态 |
| statusName | string | |
| handlerName | string | 维修员 |
| createTime | string | |
| finishTime | string | |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "page": 1, "size": 10, "total": 1, "pages": 1,
    "list": [{
      "id": 1, "orderNo": "WO20260703001", "communityName": "阳光花园小区",
      "houseNo": "1单元-3-302", "ownerName": "张三", "ownerPhone": "13800001111",
      "title": "卫生间漏水", "type": 1, "typeName": "水电",
      "priority": 3, "priorityName": "高",
      "status": 2, "statusName": "已派单",
      "handlerName": "赵维修", "createTime": "2026-07-03 09:00:00", "finishTime": null
    }]
  }
}
```

---

## 2. 工单详情（含操作日志）

`GET /api/business/workorders/{id}`

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| （列表全部字段） | | |
| description | string | 故障描述 |
| images | string[] | 图片 URL 数组 |
| handleResult | string | 处理结果 |
| handleTime | string | 处理时间 |
| rating | int | 评分 |
| ratingComment | string | 评价 |
| logs | array | 操作日志 |

`logs[]`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, orderId | | |
| operatorName | string | 操作人 |
| action | string | 动作（派单/接单/...） |
| fromStatus, toStatus | int | 状态变更 |
| remark | string | |
| createTime | string | |

---

## 3. 新增工单（业主报修）

`POST /api/business/workorders`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 是 | |
| houseId | long | 否 | 公共区域报修可不填 |
| ownerId | long | 否 | 业主自助报修取当前登录业主 |
| title | string | 是 | 标题 |
| type | int | 是 | 类型 |
| priority | int | 否 | 默认 2 |
| description | string | 否 | 故障描述 |
| images | string[] | 否 | 图片 URL |

**请求示例**：

```json
{
  "communityId": 1, "houseId": 1, "ownerId": 1,
  "title": "卫生间漏水", "type": 1, "priority": 3,
  "description": "主卧卫生间水管接口处持续滴水",
  "images": ["http://xxx/1.jpg"]
}
```

**响应示例**：

```json
{ "code": 200, "message": "报修成功", "data": { "id": 1, "orderNo": "WO20260703001" } }
```

**说明**：工单号由后端按 `WO + yyyyMMdd + 三位流水` 自动生成。

---

## 4. 修改工单

`PUT /api/business/workorders/{id}`

**简介**：仅待派单状态可改基本信息。

**请求体**：title、type、priority、description、images、houseId。

**错误码**：409（状态不允许修改）

---

## 5. 派单

`POST /api/business/workorders/{id}/assign`

**简介**：把待派单工单指派给维修员。状态 ①→②。

**权限**：`business:workorder:assign`（经理）

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| handlerId | long | 是 | 维修员用户 id |
| remark | string | 否 | 派单备注 |

**响应示例**：

```json
{ "code": 200, "message": "派单成功", "data": null }
```

**错误码**：

| code | message |
| ---- | ---- |
| 404 | 工单不存在 |
| 2101 | 当前状态不允许派单 |
| 400 | 维修员不能为空 |

---

## 6. 接单

`POST /api/business/workorders/{id}/accept`

**简介**：维修员确认接单，状态 ②→③。仅被指派的维修员本人或经理可操作。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| remark | string | 否 | |

**错误码**：2102（该工单已被他人接单）/ 2101

---

## 7. 处理完成

`POST /api/business/workorders/{id}/finish`

**简介**：维修员提交处理结果，状态 ③→④。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| handleResult | string | 是 | 处理结果说明 |
| images | string[] | 否 | 处理后图片 |

**错误码**：2101（状态不允许）

---

## 8. 关闭工单

`POST /api/business/workorders/{id}/close`

**简介**：经理对已完成工单复核关闭，状态 ④→⑤。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| remark | string | 否 | |

---

## 9. 撤销工单

`POST /api/business/workorders/{id}/cancel`

**简介**：业主或经理撤销。状态 ①/②/③→⑥。已完成/已关闭不可撤销。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| reason | string | 是 | 撤销原因 |

**错误码**：2101

---

## 10. 评价

`POST /api/business/workorders/{id}/rate`

**简介**：业主对已完成/已关闭工单评分。状态不变。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| rating | int | 是 | 1-5 |
| ratingComment | string | 否 | |

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 该工单已评价过 |
| 2101 | 工单未完成，不可评价 |

---

# 二、费用管理

## 11. 收费项目列表

`GET /api/business/fee-items`

**Query**：`page`、`size`、`name`、`type`、`status`

**响应** `data`：PageResult<FeeItemVO>

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| name | string | 项目名 |
| type | int | 类型 |
| typeName | string | |
| unit | string | 单位 |
| unitPrice | number | 单价 |
| billingCycle | int | 周期 |
| status | int | |

---

## 12. 新增收费项目

`POST /api/business/fee-items`

**请求体**：name、type、unit、unitPrice、billingCycle、status。

**错误码**：409（项目名已存在）

## 13. 修改收费项目

`PUT /api/business/fee-items/{id}` — 同新增。

## 14. 删除收费项目

`DELETE /api/business/fee-items/{id}`

**错误码**：409（已有账单引用，建议停用而非删除）

---

## 15. 分页查询账单

`GET /api/business/bills`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| communityId | long | 否 | |
| buildingId | long | 否 | |
| houseId | long | 否 | |
| ownerId | long | 否 | |
| feeItemType | int | 否 | 收费类型（按 fee_item.type） |
| status | int | 否 | 1未缴 2部分 3已缴清 4已作废 |
| period | string | 否 | 账期，如 2026-07 |
| overdue | int | 否 | 1只看待欠费（status in 1,2 且 due_date < today） |

**响应** `data`（PageResult<BillVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| billNo | string | 账单号 |
| communityName, houseNo, ownerName | string | |
| feeItemName | string | 收费项目名 |
| feeItemType | int | |
| period | string | 账期 |
| quantity | number | 数量 |
| amount | number | 应收 |
| paidAmount | number | 已收 |
| unpaidAmount | number | 未收（计算字段 = amount - paidAmount） |
| status | int | |
| statusName | string | |
| dueDate | string | 截止日 |
| overdue | int | 是否欠费（0/1，计算字段） |
| createTime | string | |

---

## 16. 账单详情

`GET /api/business/bills/{id}`

**响应** `data`：BillVO 全字段 + `payments`（缴费记录数组）。

`payments[]`：paymentNo、amount、payMethodName、payTime、collectorName、remark。

---

## 17. 批量生成账单

`POST /api/business/bills/generate`

**简介**：按小区/楼栋/房屋 + 收费项目 + 账期，批量生成账单。金额 = 单价 × 数量（物业费用房屋面积，水电费按读数，车位费按月）。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 是 | |
| buildingId | long | 否 | 不传则全小区 |
| feeItemId | long | 是 | 收费项目 |
| period | string | 是 | 账期 yyyy-MM |
| dueDate | string | 否 | 缴费截止日 |
| remark | string | 否 | |

**响应示例**：

```json
{ "code": 200, "message": "生成完成", "data": { "generated": 156, "skipped": 3 } }
```

> `skipped`：已存在同房屋同项目同账期的账单，跳过，避免重复。

**错误码**：400（参数不全）/ 404（收费项目不存在）

---

## 18. 缴费

`POST /api/business/bills/{id}/pay`

**简介**：对账单缴费。可部分缴费。生成 payment 记录，更新账单 paidAmount 和 status。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| amount | number | 是 | 实缴金额 |
| payMethod | int | 是 | 1现金 2微信 3支付宝 4银行 5POS |
| remark | string | 否 | |

**响应示例**：

```json
{
  "code": 200, "message": "缴费成功",
  "data": {
    "paymentNo": "PAY20260703000001",
    "billStatus": 3,
    "paidAmount": 250.00,
    "unpaidAmount": 0
  }
}
```

**错误码**：

| code | message |
| ---- | ---- |
| 2201 | 账单已缴清 |
| 2202 | 缴费金额超过应收金额 |
| 2203 | 账单已作废 |
| 404 | 账单不存在 |

---

## 19. 作废账单

`POST /api/business/bills/{id}/void`

**简介**：仅未缴费（status=1）账单可作废。已缴费账单不可作废（需走退款流程）。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| reason | string | 是 | 作废原因 |

**错误码**：2201（账单已产生缴费，不可作废，请走冲正流程）

---

## 20. 导出账单 Excel

`GET /api/business/bills/export`

**简介**：按筛选条件导出账单列表为 Excel。响应为二进制文件。

**Query**：同 #15 查询参数。

**响应**：

- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename=bills_202607.xlsx`

**响应示例**（headers）：

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=bills_20260703.xlsx
```

---

# 三、车位管理

## 21. 分页查询车位

`GET /api/business/parking/spaces`

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| communityId | long | 否 | |
| spaceNo | string | 否 | |
| areaType | int | 否 | 1地上 2地下 |
| useType | int | 否 | 1长租 2出售 3临时 |
| status | int | 否 | 1空闲 2使用中 3已售 4已禁用 |
| ownerId | long | 否 | |
| plateNo | string | 否 | |

**响应** `data`（PageResult<ParkingSpaceVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, communityId, communityName | | |
| spaceNo | string | 车位号 |
| areaType, areaTypeName | | 区域 |
| useType, useTypeName | | 用途 |
| ownerName | string | 绑定业主 |
| plateNo | string | 车牌 |
| monthlyFee | number | 月租 |
| startDate, endDate | string | 有效期 |
| status, statusName | | |
| remark | string | |

---

## 22. 车位详情

`GET /api/business/parking/spaces/{id}` — ParkingSpaceVO 全字段。

## 23. 新增车位

`POST /api/business/parking/spaces`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 是 | |
| spaceNo | string | 是 | 小区内唯一 |
| areaType | int | 是 | |
| useType | int | 是 | |
| monthlyFee | number | 否 | |
| status | int | 否 | 默认 1 |
| remark | string | 否 | |

**错误码**：409（小区+车位号重复）

## 24. 修改车位

`PUT /api/business/parking/spaces/{id}` — 同新增。

## 25. 删除车位

`DELETE /api/business/parking/spaces/{id}`

**错误码**：409（车位使用中，不可删除）

---

## 26. 绑定业主/车牌

`POST /api/business/parking/spaces/{id}/bind`

**简介**：把空闲/已售车位绑定到业主并录入车牌，状态变更为「使用中/已售」。写 parking_record。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| ownerId | long | 是 | |
| plateNo | string | 否 | |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期（长租） |
| amount | number | 否 | 本次费用 |
| remark | string | 否 | |

**错误码**：2301（车位已被占用）

---

## 27. 解绑

`POST /api/business/parking/spaces/{id}/unbind`

**简介**：解除绑定，车位回到「空闲」。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| remark | string | 否 | |

---

## 28. 续费

`POST /api/business/parking/spaces/{id}/renew`

**简介**：长租车位续期，延长 endDate，生成续费记录。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| months | int | 是 | 续费月数 |
| amount | number | 否 | 金额（默认 months×monthlyFee） |
| remark | string | 否 | |

**响应示例**：

```json
{ "code": 200, "message": "续费成功",
  "data": { "newEndDate": "2027-07-03", "amount": 3600.00 } }
```

---

## 29. 车位操作记录

`GET /api/business/parking/records`

**Query**：`page`、`size`、`spaceId`、`ownerId`、`action`

**响应** `data`（PageResult）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, spaceId, spaceNo | | |
| ownerName, plateNo | | |
| action | string | 绑定/续费/解绑 |
| amount | number | |
| startDate, endDate | string | |
| operatorName | string | |
| createTime | string | |

---

# 四、设备管理（通闸 / 消防 / 安防）

> 三类设备统一存储（equipment.category 区分），接口统一。

| category | 名称 |
| ---- | ---- |
| 1 | 通闸设备 |
| 2 | 消防设备 |
| 3 | 安防设备 |

## 30. 分页查询设备

`GET /api/business/equipments`

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| communityId | long | 否 | |
| category | int | 否 | 1通闸 2消防 3安防 |
| name | string | 否 | 模糊 |
| code | string | 否 | |
| status | int | 否 | 1正常 2故障 3维修中 4报废 |
| onlineStatus | int | 否 | 1在线 0离线 |

**响应** `data`（PageResult<EquipmentVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, communityId, communityName | | |
| category, categoryName | | 类别 |
| name | string | 名称 |
| code | string | 编号 |
| location | string | 位置 |
| model | string | 型号 |
| manufacturer | string | 厂商 |
| installDate | string | 安装日 |
| warrantyDate | string | 保修截止 |
| onlineStatus | int | 在线 |
| status | int | 状态 |
| statusName | string | |
| lastCheckDate | string | 上次巡检 |
| nextCheckDate | string | 下次巡检 |
| remark | string | |

---

## 31. 设备详情

`GET /api/business/equipments/{id}` — EquipmentVO 全字段。

## 32. 新增设备

`POST /api/business/equipments`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 是 | |
| category | int | 是 | |
| name | string | 是 | |
| code | string | 是 | 小区+类别内唯一 |
| location | string | 否 | |
| model | string | 否 | |
| manufacturer | string | 否 | |
| installDate | string | 否 | |
| warrantyDate | string | 否 | |
| onlineStatus | int | 否 | 默认 1 |
| status | int | 否 | 默认 1 |
| nextCheckDate | string | 否 | |
| remark | string | 否 | |

**错误码**：2401（编号已存在）/ 400

## 33. 修改设备

`PUT /api/business/equipments/{id}` — 同新增。

## 34. 删除设备

`DELETE /api/business/equipments/{id}`

**错误码**：409（设备有巡检记录，建议改为「报废」状态而非删除）

---

## 35. 提交巡检

`POST /api/business/equipments/{id}/check`

**简介**：维修员/巡检员提交一次巡检记录。若异常自动把设备状态置为「故障」。更新 lastCheckDate / nextCheckDate。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| result | int | 是 | 1正常 2异常 |
| issueDesc | string | 否 | 异常描述（result=2 必填） |
| images | string[] | 否 | 现场照片 |
| nextCheckDate | string | 否 | 下次巡检日 |

**响应示例**：

```json
{ "code": 200, "message": "巡检已提交", "data": { "checkId": 88 } }
```

**错误码**：400（异常结果未填描述）

---

## 36. 巡检记录

`GET /api/business/equipments/{id}/checks`

**Query**：`page`、`size`

**响应** `data`（PageResult）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, equipmentId | | |
| checkerName | string | 巡检人 |
| checkTime | string | |
| result, resultName | | |
| issueDesc | string | |
| images | string[] | |

---

## 37. 到期/待检设备提醒

`GET /api/business/equipments/expiring`

**简介**：查询保修即将到期（warrantyDate 30 天内）或巡检到期（nextCheckDate ≤ 今天+7天）或离线设备，供仪表盘/提醒用。

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 否 | |
| type | string | 否 | `warranty`(保修到期) / `check`(巡检到期) / `offline`(离线)，默认全部 |

**响应** `data`（数组，按提醒类型分组）：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "warrantyExpiring": [
      { "id": 5, "name": "1号楼门禁", "code": "G001", "categoryName": "通闸", "warrantyDate": "2026-07-25", "daysLeft": 22 }
    ],
    "checkExpiring": [
      { "id": 8, "name": "B区监控", "code": "C003", "categoryName": "安防", "nextCheckDate": "2026-07-09", "daysLeft": 6 }
    ],
    "offline": [
      { "id": 12, "name": "地下车库消防栓", "code": "F010", "categoryName": "消防" }
    ]
  }
}
```

---

## 相关文档

- [04 - 接口规范文档](./04-接口规范文档.md)
- [API-03 - 基础数据接口文档](./API-03-基础数据接口文档.md)
- [API-05 - 数据统计接口文档](./API-05-数据统计接口文档.md)
