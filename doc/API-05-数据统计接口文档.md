# API-05 - 数据统计接口文档（仪表盘 / 报表 / 导出）

> 模块：数据统计
> Base URL：`/api` ｜ 全部接口需登录鉴权 ｜ 通用约定见 [04 接口规范文档](./04-接口规范文档.md)

## 接口清单

| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 1 | GET | /stat/dashboard | stat:dashboard:view |
| 2 | GET | /stat/charge/summary | stat:charge:view |
| 3 | GET | /stat/charge/trend | stat:charge:view |
| 4 | GET | /stat/charge/by-type | stat:charge:view |
| 5 | GET | /stat/workorder/summary | stat:workorder:view |
| 6 | GET | /stat/workorder/by-status | stat:workorder:view |
| 7 | GET | /stat/workorder/by-handler | stat:workorder:view |
| 8 | GET | /stat/parking/usage | stat:parking:view |
| 9 | GET | /stat/equipment/status | stat:equipment:view |
| 10 | GET | /stat/export | stat:export |

> 统计接口通用 Query 参数：
> - `communityId` (long, 否)：不传则按当前用户所属小区；超管可查全部
> - `startDate` / `endDate` (string, 否)：统计时间区间 `yyyy-MM-dd`

---

## 1. 仪表盘概览

`GET /api/stat/dashboard`

**简介**：首页仪表盘关键指标卡 + 待办提醒。

**Query**：`communityId`（默认本月数据）。

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| houseTotal | int | 房屋总数 |
| ownerTotal | int | 业主总数 |
| checkInRate | number | 入住率 %（status=3 占比） |
| monthReceivable | number | 本月应收（账单 amount 合计） |
| monthReceived | number | 本月实收（payment amount 合计） |
| monthUnpaid | number | 本月未收（应收-实收） |
| collectionRate | number | 本月收缴率 % |
| workorderPending | int | 待处理工单数（status in 1,2,3） |
| workorderMonthTotal | int | 本月工单数 |
| equipmentOnlineRate | number | 设备在线率 % |
| todoList | array | 待办提醒 |

`todoList[]`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| type | string | `overdue_bill`(欠费) / `workorder`(待处理工单) / `equipment`(设备到期) / `parking`(车位到期) |
| count | int | 数量 |
| desc | string | 描述 |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "houseTotal": 200, "ownerTotal": 168, "checkInRate": 84.0,
    "monthReceivable": 58000.00, "monthReceived": 42300.00,
    "monthUnpaid": 15700.00, "collectionRate": 72.93,
    "workorderPending": 5, "workorderMonthTotal": 18,
    "equipmentOnlineRate": 96.5,
    "todoList": [
      { "type": "overdue_bill", "count": 12, "desc": "12 笔账单已逾期" },
      { "type": "workorder", "count": 5, "desc": "5 个工单待处理" },
      { "type": "equipment", "count": 3, "desc": "3 台设备待巡检/将过保" }
    ]
  }
}
```

---

## 2. 收费汇总

`GET /api/stat/charge/summary`

**简介**：按条件统计应收/实收/欠费，用于报表卡片。

**Query**：`communityId`、`buildingId`(否)、`feeItemType`(否)、`startDate`、`endDate`、`period`(否，如 2026-07)

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| receivable | number | 应收合计 |
| received | number | 实收合计 |
| unpaid | number | 欠费合计 |
| collectionRate | number | 收缴率 % |
| billCount | int | 账单总数 |
| paidCount | int | 已缴清账单数 |
| overdueCount | int | 逾期账单数 |

---

## 3. 收费趋势（折线图）

`GET /api/stat/charge/trend`

**简介**：按月统计近 N 个月的应收/实收，供 ECharts 折线图。

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 否 | |
| months | int | 否 | 近几个月，默认 6 |
| feeItemType | int | 否 | |

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| categories | string[] | X 轴月份，如 `["2026-02","2026-03",...]` |
| receivable | number[] | 应收序列 |
| received | number[] | 实收序列 |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "categories": ["2026-02","2026-03","2026-04","2026-05","2026-06","2026-07"],
    "receivable": [56000,56000,56000,58000,58000,58000],
    "received":    [52000,54000,51000,56000,55000,42300]
  }
}
```

---

## 4. 按费用类型占比（饼图）

`GET /api/stat/charge/by-type`

**简介**：统计区间内各费用类型（物业费/水费/电费/车位费）实收占比，供饼图。

**Query**：`communityId`、`startDate`、`endDate`

**响应** `data`（数组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| name | string | 费用类型名（物业费/水费…） |
| value | number | 实收金额 |
| percent | number | 占比 % |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    { "name": "物业费", "value": 30000.00, "percent": 70.9 },
    { "name": "水费",   "value":  5000.00, "percent": 11.8 },
    { "name": "电费",   "value":  4800.00, "percent": 11.3 },
    { "name": "车位费", "value":  2500.00, "percent":  5.9 }
  ]
}
```

---

## 5. 工单汇总

`GET /api/stat/workorder/summary`

**Query**：`communityId`、`startDate`、`endDate`

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| total | int | 工单总数 |
| finished | int | 已完成 |
| handling | int | 处理中（含已派单） |
| pending | int | 待派单 |
| canceled | int | 已撤销 |
| avgHandleHours | number | 平均处理时长（小时） |
| avgRating | number | 平均评分 |

---

## 6. 工单状态分布（饼图/柱状）

`GET /api/stat/workorder/by-status`

**响应** `data`（数组，按状态聚合）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| status | int | 状态值 |
| name | string | 状态名 |
| count | int | 数量 |

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    { "status": 1, "name": "待派单", "count": 2 },
    { "status": 2, "name": "已派单", "count": 3 },
    { "status": 3, "name": "处理中", "count": 1 },
    { "status": 4, "name": "已完成", "count": 10 },
    { "status": 5, "name": "已关闭", "count": 8 },
    { "status": 6, "name": "已撤销", "count": 1 }
  ]
}
```

---

## 7. 维修员工单量排名

`GET /api/stat/workorder/by-handler`

**简介**：统计各维修员处理的工单数、平均时长、平均评分，供排名柱状图。

**Query**：`communityId`、`startDate`、`endDate`

**响应** `data`（数组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| handlerId | long | |
| handlerName | string | |
| finishedCount | int | 完成数 |
| avgHandleHours | number | 平均时长 |
| avgRating | number | 平均评分 |

---

## 8. 车位使用率

`GET /api/stat/parking/usage`

**简介**：车位总数 / 使用中 / 空闲 / 已售，分地上地下。

**Query**：`communityId`

**响应** `data`：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| total | int | 车位总数 |
| inUse | int | 使用中 |
| free | int | 空闲 |
| sold | int | 已售 |
| usageRate | number | 使用率 % |
| byArea | array | 按区域分组 |

`byArea[]`：areaType、areaName、total、inUse、usageRate。

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "total": 150, "inUse": 120, "free": 25, "sold": 5, "usageRate": 80.0,
    "byArea": [
      { "areaType": 1, "areaName": "地上", "total": 50, "inUse": 40, "usageRate": 80.0 },
      { "areaType": 2, "areaName": "地下", "total": 100, "inUse": 80, "usageRate": 80.0 }
    ]
  }
}
```

---

## 9. 设备状态统计

`GET /api/stat/equipment/status`

**简介**：按类别统计设备数量、正常/故障/维修/报废、在线率。

**Query**：`communityId`

**响应** `data`（按类别分组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| category | int | |
| categoryName | string | |
| total | int | |
| normal | int | 正常 |
| fault | int | 故障 |
| repairing | int | 维修中 |
| scrapped | int | 报废 |
| onlineRate | number | 在线率 % |

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    { "category": 1, "categoryName": "通闸设备", "total": 8, "normal": 7, "fault": 1, "repairing": 0, "scrapped": 0, "onlineRate": 100.0 },
    { "category": 2, "categoryName": "消防设备", "total": 30, "normal": 30, "fault": 0, "repairing": 0, "scrapped": 0, "onlineRate": 96.7 },
    { "category": 3, "categoryName": "安防设备", "total": 45, "normal": 44, "fault": 1, "repairing": 0, "scrapped": 0, "onlineRate": 97.8 }
  ]
}
```

---

## 10. 综合报表导出

`GET /api/stat/export`

**简介**：导出综合统计报表（多 sheet：收费汇总、工单汇总、车位、设备）为 Excel。

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| type | string | 是 | `charge` / `workorder` / `parking` / `equipment` |
| communityId | long | 否 | |
| startDate | string | 否 | |
| endDate | string | 否 | |

**响应**：二进制 Excel 文件。

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=charge_report_202607.xlsx
```

**错误码**：400（type 不合法）

---

## 附：统计 SQL 示例（参考实现）

为帮助后端实现，给出几个核心统计 SQL：

**本月收缴率：**
```sql
SELECT
  SUM(amount)       AS receivable,
  SUM(paid_amount)  AS received,
  SUM(amount - paid_amount) AS unpaid,
  ROUND(SUM(paid_amount)/NULLIF(SUM(amount),0)*100, 2) AS collection_rate
FROM bill
WHERE deleted = 0 AND status IN (1,2,3)
  AND DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m');
```

**工单状态分布：**
```sql
SELECT status, COUNT(*) AS count
FROM work_order
WHERE deleted = 0
GROUP BY status
ORDER BY status;
```

**维修员排名：**
```sql
SELECT u.real_name AS handler_name,
       COUNT(*) AS finished_count,
       ROUND(AVG(TIMESTAMPDIFF(HOUR, handle_time, finish_time)),1) AS avg_handle_hours,
       ROUND(AVG(rating),1) AS avg_rating
FROM work_order w JOIN sys_user u ON w.handler_id = u.id
WHERE w.deleted = 0 AND w.status >= 4 AND w.rating IS NOT NULL
GROUP BY w.handler_id, u.real_name
ORDER BY finished_count DESC;
```

## 相关文档

- [04 - 接口规范文档](./04-接口规范文档.md)
- [API-04 - 业务功能接口文档](./API-04-业务功能接口文档.md)
