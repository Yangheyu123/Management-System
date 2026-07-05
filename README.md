# 小区物业管理系统（全栈）

一套面向住宅小区物业运营的 B 端管理系统，覆盖「人、房、车、钱、事、设备」六大维度，按 `doc/` 下完整文档实现，**无 mock 数据**，全部连接真实 MySQL。

- 后端：SpringBoot 2.7.18 + MyBatis（注解 SQL）+ JWT + RBAC + Apache POI
- 前端：Vue 3 + Vite + Element Plus + Pinia + Vue Router + ECharts
- 数据库：MySQL 8（库 `property`，19 张表）

## 目录结构

```
project/
├── db/
│   ├── schema.sql          # 19 张表 DDL
│   └── data.sql            # 种子数据（角色/权限/用户占位/小区/楼栋/房屋/业主/账单/工单/车位/设备）
├── backend/                # SpringBoot 后端（com.demo）
│   └── src/main/java/com/demo/{controller,service,mapper,entity,common,config,security,utils,...}
├── frontend/               # Vue3 前端
│   └── src/{api,views,layout,router,stores,utils,components,styles}
└── doc/                    # 需求/架构/数据库/接口/部署 文档（5 模块 96 接口）
```

## 快速开始

### 1. 初始化数据库（必做）

```bash
mysql -u root -p < db/schema.sql      # 建库 property + 19 张表
mysql -u root -p property < db/data.sql  # 写入种子数据
```

> 密码见 `backend/src/main/resources/application.yml`（默认 `fbec6187`，按需修改）。
> 种子用户密码统一写 `INIT`，**后端首次启动时由 `DataInitializer` 用 BCrypt 自动写入正式密码**，无需手算哈希。

### 2. 启动后端（8080）

```bash
cd backend
mvn spring-boot:run          # 或 IDEA 运行 Application.java
```

### 3. 启动前端（5173，自动代理 /api → 8080）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

## 预置账号

| 账号 | 密码 | 角色 |
| ---- | ---- | ---- |
| admin | admin123 | 超级管理员（全部权限） |
| manager01 | 123456 | 物业经理 |
| toll01 | 123456 | 收费员 |
| repair01 | 123456 | 维修员 |
| owner01 | 123456 | 业主 |

## 功能模块

- **用户认证**：登录/注销/刷新 Token/改密，JWT 拦截器全局鉴权，RBAC 按钮级权限（`@RequirePermission` + 前端 `v-permission`）。
- **系统管理**：用户/角色/权限 CRUD，角色分配权限，用户分配角色，权限树。
- **基础数据**：小区/楼栋/房屋/业主，房屋-业主多对多，身份证脱敏。
- **业务功能**：工单状态机（派单/接单/完成/关闭/撤销/评价 + 日志）、费用项目、账单（批量生成/部分缴费/作废/Excel 导出）、车位（绑定/解绑/续费 + 记录）、设备（通闸/消防/安防统一 + 巡检 + 到期提醒）。
- **数据统计**：仪表盘、收费汇总/趋势/类型占比、工单汇总/状态分布/维修员排名、车位使用率、设备状态，全部基于真实 SQL 聚合 + ECharts 可视化。

## 设计说明

前端引入 [taste-skill](https://github.com/Leonxlnx/taste-skill) 的设计理念（其 SKILL 明确声明 dashboard/admin 不在其核心范围，仅适用其反 slop 的 token 规范）：

- 单一强调色（克制商务蓝 `#2f6fed`），不用 AI 紫与霓虹
- 统一圆角（卡片 12px / 控件 8px）
- 中性灰阶（Slate）底色，状态用语义化 tag 着色
- 登录页采用 premium/克制的左右分屏，遵守 WCAG 按钮对比度
- 尊重 `prefers-reduced-motion`

## 文档

详见 `doc/`：项目说明、系统架构、数据库设计、接口规范、API-01~05 各模块接口、部署运行。
