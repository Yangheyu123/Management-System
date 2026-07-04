# API-03 - 基础数据接口文档（小区 / 楼栋 / 房屋 / 业主）

> 模块：基础数据
> Base URL：`/api` ｜ 全部接口需登录鉴权 ｜ 通用约定见 [04 接口规范文档](./04-接口规范文档.md)

## 接口清单

**小区**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 1 | GET | /basedata/communities | basedata:community:list |
| 2 | GET | /basedata/communities/all | basedata:community:list |
| 3 | GET | /basedata/communities/{id} | basedata:community:query |
| 4 | POST | /basedata/communities | basedata:community:add |
| 5 | PUT | /basedata/communities/{id} | basedata:community:edit |
| 6 | DELETE | /basedata/communities/{id} | basedata:community:delete |

**楼栋**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 7 | GET | /basedata/buildings | basedata:building:list |
| 8 | GET | /basedata/buildings/{id} | basedata:building:query |
| 9 | POST | /basedata/buildings | basedata:building:add |
| 10 | PUT | /basedata/buildings/{id} | basedata:building:edit |
| 11 | DELETE | /basedata/buildings/{id} | basedata:building:delete |

**房屋**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 12 | GET | /basedata/houses | basedata:house:list |
| 13 | GET | /basedata/houses/{id} | basedata:house:query |
| 14 | POST | /basedata/houses | basedata:house:add |
| 15 | PUT | /basedata/houses/{id} | basedata:house:edit |
| 16 | DELETE | /basedata/houses/{id} | basedata:house:delete |
| 17 | GET | /basedata/houses/{id}/owners | basedata:house:query |

**业主**
| # | 方法 | 路径 | 权限码 |
| -- | ---- | ---- | ---- |
| 18 | GET | /basedata/owners | basedata:owner:list |
| 19 | GET | /basedata/owners/{id} | basedata:owner:query |
| 20 | POST | /basedata/owners | basedata:owner:add |
| 21 | PUT | /basedata/owners/{id} | basedata:owner:edit |
| 22 | DELETE | /basedata/owners/{id} | basedata:owner:delete |
| 23 | GET | /basedata/owners/{id}/houses | basedata:owner:query |

---

# 一、小区管理

## 1. 分页查询小区

`GET /api/basedata/communities`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | 分页 |
| name | string | 否 | 名称模糊 |
| address | string | 否 | 地址模糊 |
| status | int | 否 | 1启用 0停用 |

**响应** `data`（PageResult<CommunityVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| name | string | 小区名 |
| address | string | 地址 |
| area | number | 占地面积 |
| greenRate | number | 绿化率 |
| buildYear | int | 建成年份 |
| developer | string | 开发商 |
| totalBuildings | int | 楼栋总数 |
| totalHouses | int | 房屋总数 |
| contactName | string | 联系人 |
| contactPhone | string | 联系电话 |
| status | int | |
| createTime | string | |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "page": 1, "size": 10, "total": 1, "pages": 1,
    "list": [{
      "id": 1, "name": "阳光花园小区", "address": "北京市朝阳区阳光路1号",
      "area": 50000.00, "greenRate": 35.00, "buildYear": 2018,
      "developer": "阳光地产", "totalBuildings": 5, "totalHouses": 200,
      "contactName": "王经理", "contactPhone": "13800000001",
      "status": 1, "createTime": "2026-06-01 09:00:00"
    }]
  }
}
```

---

## 2. 全部小区（下拉）

`GET /api/basedata/communities/all`

**简介**：不分页，返回精简字段，供下拉框。

**响应** `data`：

```json
[
  { "id": 1, "name": "阳光花园小区" },
  { "id": 2, "name": "幸福里小区" }
]
```

---

## 3. 小区详情

`GET /api/basedata/communities/{id}`

**响应** `data`：CommunityVO 全字段。

---

## 4. 新增小区

`POST /api/basedata/communities`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| name | string | 是 | 小区名，唯一 |
| address | string | 否 | |
| area | number | 否 | |
| greenRate | number | 否 | |
| buildYear | int | 否 | |
| developer | string | 否 | |
| totalBuildings | int | 否 | |
| totalHouses | int | 否 | |
| contactName | string | 否 | |
| contactPhone | string | 否 | |

**错误码**：409（小区名已存在）/ 400

---

## 5. 修改小区

`PUT /api/basedata/communities/{id}`

**请求体**：同新增。

**错误码**：404 / 409

---

## 6. 删除小区

`DELETE /api/basedata/communities/{id}`

**简介**：删除前校验该小区下无楼栋。

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 该小区下存在楼栋，请先删除楼栋 |

---

# 二、楼栋管理

## 7. 分页查询楼栋

`GET /api/basedata/buildings`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| communityId | long | 否 | 所属小区 |
| name | string | 否 | 楼栋名模糊 |
| buildingNo | string | 否 | 编号 |

**响应** `data`（PageResult<BuildingVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, communityId, communityName | | |
| name | string | 楼栋名 |
| buildingNo | string | 编号 |
| floors, units, elevators | int | 层数/单元/电梯 |
| structureType | string | 结构 |
| remark | string | |
| houseCount | int | 房屋数（统计） |
| createTime | string | |

---

## 8. 楼栋详情

`GET /api/basedata/buildings/{id}`

**响应** `data`：BuildingVO 全字段。

---

## 9. 新增楼栋

`POST /api/basedata/buildings`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 是 | |
| name | string | 是 | 如「1号楼」 |
| buildingNo | string | 是 | 小区内唯一 |
| floors, units, elevators | int | 否 | |
| structureType | string | 否 | |
| remark | string | 否 | |

**错误码**：409（小区+编号重复）/ 400 / 404（小区不存在）

---

## 10. 修改楼栋

`PUT /api/basedata/buildings/{id}` — 同新增。

## 11. 删除楼栋

`DELETE /api/basedata/buildings/{id}`

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 该楼栋下存在房屋，请先删除房屋 |

---

# 三、房屋管理

## 12. 分页查询房屋

`GET /api/basedata/houses`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| communityId | long | 否 | |
| buildingId | long | 否 | |
| houseNo | string | 否 | 房号模糊 |
| status | int | 否 | 1空置 2已售 3已入住 4装修中 |
| hasOwner | int | 否 | 1只看已绑定业主 |

**响应** `data`（PageResult<HouseVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id, communityId, communityName, buildingId, buildingName | | |
| houseNo | string | 房号 |
| unitNo | string | 单元 |
| floorNo | int | 楼层 |
| area | number | 面积 |
| layout | string | 户型 |
| status | int | 状态 |
| statusName | string | 状态文案（如「已入住」） |
| ownerNames | string | 业主名（逗号拼接，便于列表展示） |
| remark | string | |
| createTime | string | |

---

## 13. 房屋详情

`GET /api/basedata/houses/{id}`

**响应** `data`：HouseVO 全字段。

---

## 14. 新增房屋

`POST /api/basedata/houses`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| communityId | long | 是 | |
| buildingId | long | 是 | |
| houseNo | string | 是 | 楼栋内唯一 |
| unitNo | string | 否 | |
| floorNo | int | 否 | |
| area | number | 否 | |
| layout | string | 否 | |
| status | int | 否 | 默认 1 |
| remark | string | 否 | |

**错误码**：409（楼栋+房号重复）/ 400

---

## 15. 修改房屋

`PUT /api/basedata/houses/{id}` — 同新增。

## 16. 删除房屋

`DELETE /api/basedata/houses/{id}`

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 该房屋存在未结清账单或进行中工单，不可删除 |

---

## 17. 房屋下业主列表

`GET /api/basedata/houses/{id}/owners`

**响应** `data`（数组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| ownerId | long | |
| name | string | 姓名 |
| phone | string | |
| relation | string | 与户主关系 |
| isPrimary | int | 1户主 0成员 |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    { "ownerId": 1, "name": "张三", "phone": "13800001111", "relation": "户主", "isPrimary": 1 },
    { "ownerId": 2, "name": "李四", "phone": "13800002222", "relation": "配偶", "isPrimary": 0 }
  ]
}
```

---

# 四、业主管理

## 18. 分页查询业主

`GET /api/basedata/owners`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| page / size | int | 否 | |
| name | string | 否 | 模糊 |
| phone | string | 否 | 模糊 |
| idCard | string | 否 | |
| plateNo | string | 否 | 车牌 |
| status | int | 否 | 1在住 2已搬离 |

**响应** `data`（PageResult<OwnerVO>）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | long | |
| name | string | 姓名 |
| phone | string | 手机 |
| idCard | string | 身份证（脱敏：前6后4） |
| gender | int | |
| plateNo | string | 车牌 |
| moveInDate | string | 入住日期 |
| status | int | |
| houseCount | int | 名下房屋数 |
| houseNames | string | 房屋（拼接） |
| remark | string | |
| createTime | string | |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "page": 1, "size": 10, "total": 1, "pages": 1,
    "list": [{
      "id": 1, "name": "张三", "phone": "13800001111",
      "idCard": "110101********1234", "gender": 1,
      "plateNo": "京A12345", "moveInDate": "2018-05-01",
      "status": 1, "houseCount": 1, "houseNames": "1号楼-1-101",
      "remark": null, "createTime": "2018-05-01 10:00:00"
    }]
  }
}
```

> **注意**：身份证号在列表/详情接口返回时需脱敏（保留前6后4，中间用 `*`），仅特定权限（如 `basedata:owner:viewIdCard`）可看完整。

---

## 19. 业主详情

`GET /api/basedata/owners/{id}`

**响应** `data`：OwnerVO 全字段 + `houses`（名下房屋详情数组）。

---

## 20. 新增业主

`POST /api/basedata/owners`

**简介**：新增业主时可同时绑定房屋（传 houseIds）。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| name | string | 是 | |
| phone | string | 是 | |
| idCard | string | 否 | 唯一 |
| gender | int | 否 | |
| plateNo | string | 否 | |
| moveInDate | string | 否 | yyyy-MM-dd |
| status | int | 否 | 默认 1 |
| remark | string | 否 | |
| houseBindings | array | 否 | 绑定房屋 [{houseId, relation, isPrimary}] |

**请求示例**：

```json
{
  "name": "王五",
  "phone": "13800003333",
  "idCard": "110101199001011234",
  "gender": 1,
  "plateNo": "京B66666",
  "moveInDate": "2026-07-01",
  "houseBindings": [{ "houseId": 1, "relation": "户主", "isPrimary": 1 }]
}
```

**错误码**：409（身份证已存在）/ 400

---

## 21. 修改业主

`PUT /api/basedata/owners/{id}`

**请求体**：name、phone、idCard、gender、plateNo、moveInDate、status、remark。

## 22. 删除业主

`DELETE /api/basedata/owners/{id}`

**简介**：逻辑删除，并解除其房屋绑定（owner_house）。

**错误码**：

| code | message |
| ---- | ---- |
| 409 | 该业主存在未结清账单，不可删除 |

---

## 23. 业主名下房屋

`GET /api/basedata/owners/{id}/houses`

**响应** `data`（数组）：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| houseId | long | |
| houseNo | string | 房号 |
| buildingName | string | 楼栋 |
| communityName | string | 小区 |
| area | number | 面积 |
| relation | string | 关系 |
| isPrimary | int | |

**响应示例**：

```json
{
  "code": 200, "message": "操作成功",
  "data": [
    { "houseId": 1, "houseNo": "1单元-3-302", "buildingName": "1号楼", "communityName": "阳光花园小区", "area": 89.5, "relation": "户主", "isPrimary": 1 }
  ]
}
```

---

## 附：数据权限说明

- 非超管、非经理角色，默认只能看到自己 `communityId` 下的数据
- 业主角色只能看到与本人关联的房屋 / 账单 / 工单（在各业务接口内部过滤）

## 相关文档

- [04 - 接口规范文档](./04-接口规范文档.md)
- [API-04 - 业务功能接口文档](./API-04-业务功能接口文档.md)
