-- ============================================================
-- 小区物业管理系统 - 种子数据
-- 依赖 schema.sql 已建表。执行：mysql -u root -p property < data.sql
-- 说明：用户密码字段统一先写 'INIT'，由后端 DataInitializer 启动时
--       用 BCrypt 加密为正式密码（admin/admin123，其余 123456），保证可登录。
-- ============================================================
USE property;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空（保证可重复执行）
TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE community;
TRUNCATE TABLE building;
TRUNCATE TABLE house;
TRUNCATE TABLE owner;
TRUNCATE TABLE owner_house;
TRUNCATE TABLE work_order_log;
TRUNCATE TABLE work_order;
TRUNCATE TABLE fee_item;
TRUNCATE TABLE payment;
TRUNCATE TABLE bill;
TRUNCATE TABLE parking_record;
TRUNCATE TABLE parking_space;
TRUNCATE TABLE equipment_check;
TRUNCATE TABLE equipment;
SET FOREIGN_KEY_CHECKS = 1;

-- ===================== 角色 =====================
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '超级管理员', 'SUPER_ADMIN',     '拥有全部权限', 1),
(2, '物业经理',   'PROPERTY_MANAGER','管理本小区全部业务', 1),
(3, '收费员',     'TOLL_COLLECTOR',  '费用管理、收费', 1),
(4, '维修员',     'REPAIRMAN',       '接单处理工单', 1),
(5, '业主',       'OWNER',           '仅看本人数据', 1);

-- ===================== 权限（菜单 + 按钮） =====================
-- 一级菜单
INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, type, path, icon, sort) VALUES
(1,   0, '仪表盘',   'dashboard',  1, '/dashboard',        'Odometer',     1),
(100, 0, '基础数据', 'basedata',   1, '/basedata',         'OfficeBuilding', 2),
(200, 0, '业务管理', 'business',   1, '/business',         'Briefcase',    3),
(300, 0, '数据统计', 'stat',       1, '/stat',             'TrendCharts',  4),
(400, 0, '系统管理', 'system',     1, '/system',           'Setting',      5);

-- 二级菜单
INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, type, path, icon, sort) VALUES
(101, 100, '小区管理', 'basedata:community', 1, '/basedata/community', 'House',      1),
(102, 100, '楼栋管理', 'basedata:building',  1, '/basedata/building',  'School',     2),
(103, 100, '房屋管理', 'basedata:house',     1, '/basedata/house',     'HomeFilled', 3),
(104, 100, '业主管理', 'basedata:owner',     1, '/basedata/owner',     'User',       4),

(201, 200, '工单管理', 'business:workorder', 1, '/business/workorder', 'Tools', 1),
(202, 200, '费用管理', 'business:bill',      1, '/business/bill',      'Money', 2),
(203, 200, '车位管理', 'business:parking',   1, '/business/parking',   'Van',   3),
(204, 200, '设备管理', 'business:equipment', 1, '/business/equipment', 'Monitor', 4),

(301, 300, '收费报表', 'stat:charge',    1, '/stat/charge',    'Wallet',      1),
(302, 300, '工单报表', 'stat:workorder', 1, '/stat/workorder', 'Tickets',     2),
(303, 300, '车位报表', 'stat:parking',   1, '/stat/parking',   'Van',         3),
(304, 300, '设备报表', 'stat:equipment', 1, '/stat/equipment', 'Cpu',         4),

(401, 400, '用户管理', 'system:user',       1, '/system/user',       'UserFilled', 1),
(402, 400, '角色管理', 'system:role',       1, '/system/role',       'Avatar',     2),
(403, 400, '权限管理', 'system:permission', 1, '/system/permission', 'Key',        3);

-- 按钮（type=2）。批量插入各模块 list/add/edit/delete/query 及关键动作
INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, type, sort) VALUES
-- 小区
(1011,101,'查询','basedata:community:list',2,1),(1012,101,'详情','basedata:community:query',2,2),
(1013,101,'新增','basedata:community:add',2,3),(1014,101,'修改','basedata:community:edit',2,4),(1015,101,'删除','basedata:community:delete',2,5),
-- 楼栋
(1021,102,'查询','basedata:building:list',2,1),(1022,102,'详情','basedata:building:query',2,2),
(1023,102,'新增','basedata:building:add',2,3),(1024,102,'修改','basedata:building:edit',2,4),(1025,102,'删除','basedata:building:delete',2,5),
-- 房屋
(1031,103,'查询','basedata:house:list',2,1),(1032,103,'详情','basedata:house:query',2,2),
(1033,103,'新增','basedata:house:add',2,3),(1034,103,'修改','basedata:house:edit',2,4),(1035,103,'删除','basedata:house:delete',2,5),
-- 业主
(1041,104,'查询','basedata:owner:list',2,1),(1042,104,'详情','basedata:owner:query',2,2),
(1043,104,'新增','basedata:owner:add',2,3),(1044,104,'修改','basedata:owner:edit',2,4),(1045,104,'删除','basedata:owner:delete',2,5),
-- 工单
(2011,201,'查询','business:workorder:list',2,1),(2012,201,'详情','business:workorder:query',2,2),
(2013,201,'新增','business:workorder:add',2,3),(2014,201,'修改','business:workorder:edit',2,4),
(2015,201,'派单','business:workorder:assign',2,5),(2016,201,'接单','business:workorder:accept',2,6),
(2017,201,'完成','business:workorder:finish',2,7),(2018,201,'关闭','business:workorder:close',2,8),
(2019,201,'撤销','business:workorder:cancel',2,9),(2020,201,'评价','business:workorder:rate',2,10),
-- 费用
(2021,202,'收费项目','business:fee:list',2,1),(2022,202,'项目新增','business:fee:add',2,2),
(2023,202,'项目修改','business:fee:edit',2,3),(2024,202,'项目删除','business:fee:delete',2,4),
(2025,202,'账单查询','business:bill:list',2,5),(2026,202,'账单详情','business:bill:query',2,6),
(2027,202,'生成账单','business:bill:generate',2,7),(2028,202,'缴费','business:bill:pay',2,8),
(2029,202,'作废','business:bill:void',2,9),(2030,202,'导出','business:bill:export',2,10),
-- 车位
(2031,203,'查询','business:parking:list',2,1),(2032,203,'详情','business:parking:query',2,2),
(2033,203,'新增','business:parking:add',2,3),(2034,203,'修改','business:parking:edit',2,4),(2035,203,'删除','business:parking:delete',2,5),
(2036,203,'绑定','business:parking:bind',2,6),(2037,203,'解绑','business:parking:unbind',2,7),(2038,203,'续费','business:parking:renew',2,8),
-- 设备
(2041,204,'查询','business:equipment:list',2,1),(2042,204,'详情','business:equipment:query',2,2),
(2043,204,'新增','business:equipment:add',2,3),(2044,204,'修改','business:equipment:edit',2,4),(2045,204,'删除','business:equipment:delete',2,5),
(2046,204,'巡检','business:equipment:check',2,6),
-- 统计
(3011,301,'查看','stat:charge:view',2,1),(3012,301,'导出','stat:export',2,2),
(3021,302,'查看','stat:workorder:view',2,1),
(3031,303,'查看','stat:parking:view',2,1),
(3041,304,'查看','stat:equipment:view',2,1),
(3013,1,'查看','stat:dashboard:view',2,1),
-- 系统
(4011,401,'查询','system:user:list',2,1),(4012,401,'详情','system:user:query',2,2),
(4013,401,'新增','system:user:add',2,3),(4014,401,'修改','system:user:edit',2,4),(4015,401,'删除','system:user:delete',2,5),
(4016,401,'重置密码','system:user:reset',2,6),(4017,401,'分配角色','system:user:assign',2,7),
(4021,402,'查询','system:role:list',2,1),(4022,402,'详情','system:role:query',2,2),
(4023,402,'新增','system:role:add',2,3),(4024,402,'修改','system:role:edit',2,4),(4025,402,'删除','system:role:delete',2,5),
(4026,402,'分配权限','system:role:assign',2,6),
(4031,403,'查询','system:perm:list',2,1),(4032,403,'新增','system:perm:add',2,2),
(4033,403,'修改','system:perm:edit',2,3),(4034,403,'删除','system:perm:delete',2,4);

-- ===================== 用户（密码由 DataInitializer 写入） =====================
INSERT INTO sys_user (id, username, password, real_name, phone, email, gender, user_type, community_id, status, create_by) VALUES
(1, 'admin',    'INIT', '超级管理员', '13800000000', 'admin@property.dev', 1, 1, NULL, 1, 'system'),
(2, 'manager01','INIT', '王经理',     '13800000001', 'manager@property.dev',1, 1, 1, 1, 'system'),
(3, 'toll01',   'INIT', '李收费',     '13800000002', 'toll@property.dev',   2, 1, 1, 1, 'system'),
(4, 'repair01', 'INIT', '赵维修',     '13800000003', 'repair@property.dev', 1, 1, 1, 1, 'system'),
(5, 'owner01',  'INIT', '张业主',     '13800001111', 'owner01@property.dev',1, 2, 1, 1, 'system');

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5);

-- ===================== 角色-权限 =====================
-- 超管：代码层面通配，这里也给全部
INSERT INTO sys_role_permission (role_id, permission_id) SELECT 1, id FROM sys_permission;
-- 物业经理：全部业务+基础数据+统计
INSERT INTO sys_role_permission (role_id, permission_id) SELECT 2, id FROM sys_permission;
-- 收费员：仪表盘 + 费用/账单 + 收费统计
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission
WHERE id IN (1,3013, 202,2021,2022,2023,2024,2025,2026,2027,2028,2029,2030, 301,3011,3012);
-- 维修员：仪表盘 + 工单(接单/完成) + 设备巡检
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission
WHERE id IN (1,3013, 201,2011,2012,2016,2017, 204,2041,2042,2046);
-- 业主：仪表盘
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 5, id FROM sys_permission WHERE id IN (1,3013);

-- ===================== 小区 =====================
INSERT INTO community (id, name, address, area, green_rate, build_year, developer, total_buildings, total_houses, contact_name, contact_phone, status) VALUES
(1, '阳光花园小区', '北京市朝阳区阳光路1号', 50000.00, 35.00, 2018, '阳光地产', 5, 200, '王经理', '13800000001', 1),
(2, '幸福里小区',   '上海市浦东新区幸福路66号', 38000.00, 40.00, 2020, '幸福置地', 4, 160, '刘经理', '13800000099', 1);

-- ===================== 楼栋 =====================
INSERT INTO building (id, community_id, name, building_no, floors, units, elevators, structure_type, remark) VALUES
(1, 1, '1号楼', 'B001', 18, 2, 2, '框架', NULL),
(2, 1, '2号楼', 'B002', 24, 2, 3, '框架', NULL),
(3, 1, '3号楼', 'B003', 6,  1, 1, '砖混', NULL),
(4, 2, '1号楼', 'B001', 20, 2, 2, '框架', NULL);

-- ===================== 房屋 =====================
INSERT INTO house (id, community_id, building_id, house_no, unit_no, floor_no, area, layout, status) VALUES
(1, 1, 1, '1单元-3-302', '1单元', 3,  89.50,  '两室一厅', 3),
(2, 1, 1, '1单元-3-303', '1单元', 3,  120.00, '三室两厅', 3),
(3, 1, 1, '2单元-5-501', '2单元', 5,  76.20,  '两室一厅', 2),
(4, 1, 2, '1单元-12-1202','1单元',12, 145.80, '四室两厅', 3),
(5, 1, 2, '1单元-12-1203','1单元',12, 145.80, '四室两厅', 1),
(6, 1, 2, '2单元-8-803',  '2单元',8,  98.30,  '三室一厅', 3),
(7, 1, 3, '1单元-2-101',  '1单元',2,  60.00,  '一室一厅', 4),
(8, 1, 3, '1单元-2-102',  '1单元',2,  60.00,  '一室一厅', 1),
(9, 2, 4, '1单元-6-601',  '1单元',6,  88.00,  '两室一厅', 3),
(10,2, 4, '1单元-6-602',  '1单元',6,  110.50, '三室两厅', 2);

-- ===================== 业主 =====================
INSERT INTO owner (id, name, phone, id_card, gender, plate_no, move_in_date, status) VALUES
(1, '张伟', '13800001111', '110101198503071234', 1, '京A12345', '2018-05-01', 1),
(2, '李娜', '13800002222', '110102198811230045', 2, '京B66666', '2018-06-15', 1),
(3, '王强', '13800003333', '110103199001115567', 1, '京C77777', '2019-01-10', 1),
(4, '赵敏', '13800004444', '110104199207184489', 2, NULL,       '2020-09-01', 1),
(5, '陈杰', '13800005555', '110105199511050012', 1, '沪D88888', '2020-12-20', 1),
(6, '刘洋', '13800006666', NULL,                  1, NULL,       '2021-03-08', 2);

-- ===================== 业主-房屋 =====================
INSERT INTO owner_house (owner_id, house_id, relation, is_primary) VALUES
(1,1,'户主',1),(2,1,'配偶',0),
(3,2,'户主',1),
(1,4,'户主',1),(4,4,'子女',0),
(5,6,'户主',1),
(3,9,'户主',1);

-- ===================== 收费项目 =====================
INSERT INTO fee_item (id, name, type, unit, unit_price, billing_cycle, status) VALUES
(1, '物业费', 1, '元/㎡·月', 2.50, 1, 1),
(2, '水费',   2, '元/吨',    3.45, 1, 1),
(3, '电费',   3, '元/度',    0.58, 1, 1),
(4, '车位费', 4, '元/月',  300.00, 1, 1);

-- ===================== 账单（含已缴/未缴/部分/作废，部分逾期） =====================
INSERT INTO bill (id, bill_no, community_id, house_id, owner_id, fee_item_id, period, quantity, amount, paid_amount, status, due_date) VALUES
(1,'B20260601001',1,1,1,1,'2026-06',89.50, 223.75, 223.75, 3, '2026-06-30'),
(2,'B20260601002',1,1,1,2,'2026-06',12.00, 41.40,  41.40,  3, '2026-06-30'),
(3,'B20260601003',1,2,3,1,'2026-06',120.00,300.00, 300.00, 3, '2026-06-30'),
(4,'B20260601004',1,3,4,1,'2026-06',76.20, 190.50, 0.00,   1, '2026-06-15'),
(5,'B20260701001',1,1,1,1,'2026-07',89.50, 223.75, 0.00,   1, '2026-07-31'),
(6,'B20260701002',1,2,3,1,'2026-07',120.00,300.00, 150.00, 2, '2026-07-31'),
(7,'B20260701003',1,4,1,1,'2026-07',145.80,364.50, 0.00,   1, '2026-07-31'),
(8,'B20260701004',1,6,5,1,'2026-07',98.30, 245.75, 245.75, 3, '2026-07-31'),
(9,'B20260601005',1,5,2,1,'2026-06',145.80,364.50, 0.00,   4, '2026-06-30'),
(10,'B20260701005',1,3,4,3,'2026-07',200.00,116.00,0.00,   1, '2026-07-10'),
(11,'B20260601006',2,9,3,1,'2026-06',88.00, 220.00, 220.00, 3, '2026-06-30'),
(12,'B20260701006',2,9,3,2,'2026-07',15.00, 51.75,  0.00,   1, '2026-07-31');

-- ===================== 缴费记录 =====================
INSERT INTO payment (id, payment_no, bill_id, owner_id, amount, pay_method, pay_time, collector_id) VALUES
(1,'P20260610001',1,1,223.75,2,'2026-06-10 14:20:00',3),
(2,'P20260610002',2,1,41.40, 2,'2026-06-10 14:21:00',3),
(3,'P20260608001',3,3,300.00,3,'2026-06-08 09:30:00',3),
(4,'P20260705001',8,5,245.75,2,'2026-07-05 10:00:00',3),
(5,'P20260715001',6,3,150.00,1,'2026-07-01 16:00:00',3),
(6,'P20260620001',11,3,220.00,4,'2026-06-20 11:00:00',3);

-- ===================== 工单（覆盖各状态） =====================
INSERT INTO work_order (id, order_no, community_id, house_id, owner_id, title, type, priority, description, status, handler_id, handle_result, handle_time, finish_time, rating, rating_comment, create_time) VALUES
(1,'WO20260701001',1,1,1,'卫生间漏水',1,3,'主卧卫生间水管接口持续滴水',5,4,'已更换接口密封圈并做防水处理','2026-07-01 11:00:00','2026-07-01 15:00:00',5,'处理很及时，态度好','2026-07-01 09:30:00'),
(2,'WO20260702001',1,2,3,'客厅吊灯不亮',1,2,'吊灯突然不亮，疑似线路问题',4,4,'更换损坏开关，恢复正常',         '2026-07-02 10:00:00','2026-07-02 12:00:00',4,NULL,'2026-07-02 09:00:00'),
(3,'WO20260702002',1,4,1,'门禁无法识别',4,2,'单元门禁卡无法识别',3,4,NULL,                     NULL,                  NULL,                  NULL,NULL,'2026-07-02 14:00:00'),
(4,'WO20260703001',1,6,5,'电梯异响',    4,4,'2号楼1号电梯运行异响',2,4,NULL,                    NULL,                  NULL,                  NULL,NULL,'2026-07-03 08:30:00'),
(5,'WO20260703002',1,NULL,NULL,'小区路灯损坏',5,2,'3号楼旁路灯不亮',1,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-03 09:00:00'),
(6,'WO20260628001',1,3,4,'阳台渗水',    2,3,'下雨阳台墙面渗水',      6,4,NULL,                    NULL,                  NULL,                  NULL,NULL,'2026-06-28 10:00:00');

-- 工单日志
INSERT INTO work_order_log (order_id, operator_id, operator_name, action, from_status, to_status, remark, create_time) VALUES
(1,1,'张业主','创建',NULL,1,'业主报修','2026-07-01 09:30:00'),
(1,2,'王经理','派单',1,2,'指派给赵维修','2026-07-01 09:45:00'),
(1,4,'赵维修','接单',2,3,NULL,'2026-07-01 10:50:00'),
(1,4,'赵维修','处理完成',3,4,'更换密封圈','2026-07-01 15:00:00'),
(1,2,'王经理','关闭',4,5,NULL,'2026-07-01 16:00:00'),
(1,1,'张业主','评价',NULL,NULL,'五星好评','2026-07-01 18:00:00'),
(2,3,'王强','创建',NULL,1,'业主报修','2026-07-02 09:00:00'),
(2,2,'王经理','派单',1,2,NULL,'2026-07-02 09:15:00'),
(2,4,'赵维修','接单',2,3,NULL,'2026-07-02 10:00:00'),
(2,4,'赵维修','处理完成',3,4,'更换开关','2026-07-02 12:00:00'),
(4,2,'王经理','派单',1,2,NULL,'2026-07-03 08:40:00');

-- ===================== 车位 =====================
INSERT INTO parking_space (id, community_id, space_no, area_type, use_type, owner_id, plate_no, monthly_fee, start_date, end_date, status) VALUES
(1,1,'A-001',1,1,1,'京A12345',300.00,'2026-01-01','2026-12-31',2),
(2,1,'A-002',1,1,3,'京C77777',300.00,'2026-03-01','2026-09-30',2),
(3,1,'B-101',2,2,NULL,NULL,   60000.00,NULL,       NULL,         3),
(4,1,'B-102',2,1,5,'沪D88888',350.00,'2026-02-01','2026-08-31',2),
(5,1,'B-103',2,3,NULL,NULL,   NULL,     NULL,       NULL,         1),
(6,2,'C-001',1,1,3,'沪E99999',280.00,'2026-01-15','2027-01-14',2),
(7,1,'A-003',1,3,NULL,NULL,   NULL,     NULL,       NULL,         1);

INSERT INTO parking_record (space_id, owner_id, plate_no, action, amount, start_date, end_date, operator_id, create_time) VALUES
(1,1,'京A12345','绑定',3600.00,'2026-01-01','2026-12-31',2,'2026-01-01 10:00:00'),
(2,3,'京C77777','绑定',2100.00,'2026-03-01','2026-09-30',2,'2026-03-01 10:00:00'),
(4,5,'沪D88888','绑定',2100.00,'2026-02-01','2026-08-31',2,'2026-02-01 10:00:00'),
(6,3,'沪E99999','绑定',3360.00,'2026-01-15','2027-01-14',2,'2026-01-15 10:00:00');

-- ===================== 设备（通闸/消防/安防） =====================
INSERT INTO equipment (id, community_id, category, name, code, location, model, manufacturer, install_date, warranty_date, online_status, status, last_check_date, next_check_date) VALUES
(1,1,1,'1号楼门禁闸机','G001','1号楼入口','GM-X1','海康威视','2018-05-01','2026-07-25',1,1,'2026-06-20','2026-07-20'),
(2,1,1,'2号楼门禁闸机','G002','2号楼入口','GM-X1','海康威视','2018-05-01','2027-05-01',1,1,'2026-06-20','2026-07-20'),
(3,1,2,'地下车库消防栓','F001','地下一层','SN65','天广中茂','2018-05-01','2027-05-01',1,1,'2026-06-15','2026-07-15'),
(4,1,2,'1号楼灭火器',  'F002','1号楼走廊','MFZ4','天广中茂','2018-05-01','2026-07-10',1,2,'2026-06-10','2026-07-10'),
(5,1,2,'2号楼灭火器',  'F003','2号楼走廊','MFZ4','天广中茂','2018-05-01','2027-05-01',1,1,'2026-06-10','2026-07-10'),
(6,1,3,'A区监控摄像头','C001','A区入口','DS-2CD','海康威视','2018-05-01','2027-05-01',1,1,'2026-06-25','2026-07-25'),
(7,1,3,'B区监控摄像头','C002','B区入口','DS-2CD','海康威视','2018-05-01','2027-05-01',0,1,'2026-06-25','2026-07-25'),
(8,1,3,'电梯监控',     'C003','1号电梯','DS-2CD','海康威视','2018-05-01','2027-05-01',1,1,'2026-06-25','2026-07-09'),
(9,1,3,'地库监控',     'C004','地下一层','DS-2CD','海康威视','2018-05-01','2027-05-01',1,3,'2026-06-20','2026-07-20'),
(10,2,1,'1号楼门禁闸机','G001','1号楼入口','GM-X1','海康威视','2020-09-01','2027-09-01',1,1,'2026-06-20','2026-07-20'),
(11,2,2,'1号楼消防栓', 'F001','1号楼大厅','SN65','天广中茂','2020-09-01','2027-09-01',1,1,'2026-06-15','2026-07-15'),
(12,2,3,'小区监控',     'C001','正门','DS-2CD','海康威视','2020-09-01','2026-07-12',1,1,'2026-06-20','2026-07-12');

INSERT INTO equipment_check (equipment_id, checker_id, check_time, result, issue_desc) VALUES
(1,4,'2026-06-20 09:00:00',1,NULL),
(3,4,'2026-06-15 09:30:00',1,NULL),
(4,4,'2026-06-10 10:00:00',2,'压力表指针异常，需更换'),
(6,4,'2026-06-25 11:00:00',1,NULL),
(7,4,'2026-06-25 11:10:00',2,'设备离线，待排查网络');
