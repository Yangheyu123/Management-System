-- ============================================================
-- 小区物业管理系统 - 建库脚本（DDL）
-- 执行：mysql -u root -p < schema.sql
-- 数据库名：property  字符集：utf8mb4  引擎：InnoDB
-- ============================================================
DROP DATABASE IF EXISTS property;
CREATE DATABASE property DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE property;

SET NAMES utf8mb4;

-- ---------- 1. sys_user 系统用户 ----------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
  username        VARCHAR(50)  NOT NULL,
  password        VARCHAR(100) NOT NULL,
  real_name       VARCHAR(50)  NOT NULL,
  phone           VARCHAR(20),
  email           VARCHAR(100),
  gender          TINYINT      DEFAULT 0,
  avatar          VARCHAR(255),
  user_type       TINYINT      NOT NULL DEFAULT 1,
  community_id    BIGINT,
  status          TINYINT      NOT NULL DEFAULT 1,
  last_login_time DATETIME,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by       VARCHAR(50),
  update_by       VARCHAR(50),
  deleted         TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY uk_username (username),
  KEY idx_community (community_id),
  KEY idx_phone (phone)
) ENGINE=InnoDB COMMENT='系统用户';

-- ---------- 2. sys_role 角色 ----------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
  role_name   VARCHAR(50) NOT NULL,
  role_code   VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  status      TINYINT     NOT NULL DEFAULT 1,
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by   VARCHAR(50),
  update_by   VARCHAR(50),
  deleted     TINYINT     NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色';

-- ---------- 3. sys_permission 权限 ----------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  parent_id   BIGINT       NOT NULL DEFAULT 0,
  perm_name   VARCHAR(50)  NOT NULL,
  perm_code   VARCHAR(100) NOT NULL,
  type        TINYINT      NOT NULL,
  path        VARCHAR(200),
  icon        VARCHAR(100),
  sort        INT          NOT NULL DEFAULT 0,
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY uk_perm_code (perm_code),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB COMMENT='权限';

-- ---------- 4. sys_user_role ----------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联';

-- ---------- 5. sys_role_permission ----------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB COMMENT='角色权限关联';

-- ---------- 6. community 小区 ----------
DROP TABLE IF EXISTS community;
CREATE TABLE community (
  id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(100)  NOT NULL,
  address         VARCHAR(255),
  area            DECIMAL(10,2),
  green_rate      DECIMAL(5,2),
  build_year      INT,
  developer       VARCHAR(100),
  total_buildings INT,
  total_houses    INT,
  contact_name    VARCHAR(50),
  contact_phone   VARCHAR(20),
  status          TINYINT       NOT NULL DEFAULT 1,
  create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by       VARCHAR(50),
  update_by       VARCHAR(50),
  deleted         TINYINT       NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='小区';

-- ---------- 7. building 楼栋 ----------
DROP TABLE IF EXISTS building;
CREATE TABLE building (
  id             BIGINT      PRIMARY KEY AUTO_INCREMENT,
  community_id   BIGINT      NOT NULL,
  name           VARCHAR(50) NOT NULL,
  building_no    VARCHAR(20) NOT NULL,
  floors         INT,
  units          INT,
  elevators      INT,
  structure_type VARCHAR(20),
  remark         VARCHAR(255),
  create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by      VARCHAR(50),
  update_by      VARCHAR(50),
  deleted        TINYINT     NOT NULL DEFAULT 0,
  UNIQUE KEY uk_comm_bno (community_id, building_no)
) ENGINE=InnoDB COMMENT='楼栋';

-- ---------- 8. house 房屋 ----------
DROP TABLE IF EXISTS house;
CREATE TABLE house (
  id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
  community_id BIGINT       NOT NULL,
  building_id  BIGINT       NOT NULL,
  house_no     VARCHAR(30)  NOT NULL,
  unit_no      VARCHAR(10),
  floor_no     INT,
  area         DECIMAL(8,2),
  layout       VARCHAR(20),
  status       TINYINT      NOT NULL DEFAULT 1,
  remark       VARCHAR(255),
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    VARCHAR(50),
  update_by    VARCHAR(50),
  deleted      TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY uk_bldg_hno (building_id, house_no),
  KEY idx_community (community_id)
) ENGINE=InnoDB COMMENT='房屋';

-- ---------- 9. owner 业主 ----------
DROP TABLE IF EXISTS owner;
CREATE TABLE owner (
  id           BIGINT      PRIMARY KEY AUTO_INCREMENT,
  name         VARCHAR(50) NOT NULL,
  phone        VARCHAR(20) NOT NULL,
  id_card      VARCHAR(20),
  gender       TINYINT     DEFAULT 0,
  plate_no     VARCHAR(20),
  move_in_date DATE,
  status       TINYINT     NOT NULL DEFAULT 1,
  remark       VARCHAR(255),
  create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    VARCHAR(50),
  update_by    VARCHAR(50),
  deleted      TINYINT     NOT NULL DEFAULT 0,
  UNIQUE KEY uk_idcard (id_card),
  KEY idx_phone (phone)
) ENGINE=InnoDB COMMENT='业主';

-- ---------- 10. owner_house 业主-房屋 ----------
DROP TABLE IF EXISTS owner_house;
CREATE TABLE owner_house (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id   BIGINT NOT NULL,
  house_id   BIGINT NOT NULL,
  relation   VARCHAR(20),
  is_primary TINYINT DEFAULT 0,
  KEY idx_owner (owner_id),
  KEY idx_house (house_id)
) ENGINE=InnoDB COMMENT='业主房屋关联';

-- ---------- 11. work_order 工单 ----------
DROP TABLE IF EXISTS work_order;
CREATE TABLE work_order (
  id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
  order_no       VARCHAR(30)  NOT NULL,
  community_id   BIGINT       NOT NULL,
  house_id       BIGINT,
  owner_id       BIGINT,
  title          VARCHAR(100) NOT NULL,
  type           TINYINT      NOT NULL,
  priority       TINYINT      NOT NULL DEFAULT 2,
  description    TEXT,
  images         VARCHAR(500),
  status         TINYINT      NOT NULL DEFAULT 1,
  handler_id     BIGINT,
  handle_result  TEXT,
  handle_time    DATETIME,
  finish_time    DATETIME,
  rating         TINYINT,
  rating_comment VARCHAR(255),
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by      VARCHAR(50),
  update_by      VARCHAR(50),
  deleted        TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY uk_orderno (order_no),
  KEY idx_comm_status (community_id, status),
  KEY idx_handler (handler_id, status),
  KEY idx_owner (owner_id)
) ENGINE=InnoDB COMMENT='工单';

-- ---------- 12. work_order_log ----------
DROP TABLE IF EXISTS work_order_log;
CREATE TABLE work_order_log (
  id            BIGINT      PRIMARY KEY AUTO_INCREMENT,
  order_id      BIGINT      NOT NULL,
  operator_id   BIGINT,
  operator_name VARCHAR(50),
  action        VARCHAR(50),
  from_status   TINYINT,
  to_status     TINYINT,
  remark        VARCHAR(255),
  create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_order (order_id)
) ENGINE=InnoDB COMMENT='工单操作日志';

-- ---------- 13. fee_item 收费项目 ----------
DROP TABLE IF EXISTS fee_item;
CREATE TABLE fee_item (
  id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(50)   NOT NULL,
  type          TINYINT       NOT NULL,
  unit          VARCHAR(10),
  unit_price    DECIMAL(10,2) NOT NULL,
  billing_cycle TINYINT       NOT NULL DEFAULT 1,
  status        TINYINT       NOT NULL DEFAULT 1,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='收费项目';

-- ---------- 14. bill 账单 ----------
DROP TABLE IF EXISTS bill;
CREATE TABLE bill (
  id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
  bill_no      VARCHAR(30)   NOT NULL,
  community_id BIGINT        NOT NULL,
  house_id     BIGINT        NOT NULL,
  owner_id     BIGINT        NOT NULL,
  fee_item_id  BIGINT        NOT NULL,
  period       VARCHAR(20),
  quantity     DECIMAL(10,2) NOT NULL,
  amount       DECIMAL(12,2) NOT NULL,
  paid_amount  DECIMAL(12,2) NOT NULL DEFAULT 0,
  status       TINYINT       NOT NULL DEFAULT 1,
  due_date     DATE,
  remark       VARCHAR(255),
  create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    VARCHAR(50),
  update_by    VARCHAR(50),
  deleted      TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_billno (bill_no),
  KEY idx_house_status (house_id, status),
  KEY idx_owner_status (owner_id, status),
  KEY idx_period (period)
) ENGINE=InnoDB COMMENT='账单';

-- ---------- 15. payment 缴费记录 ----------
DROP TABLE IF EXISTS payment;
CREATE TABLE payment (
  id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
  payment_no   VARCHAR(30)   NOT NULL,
  bill_id      BIGINT        NOT NULL,
  owner_id     BIGINT        NOT NULL,
  amount       DECIMAL(12,2) NOT NULL,
  pay_method   TINYINT       NOT NULL,
  pay_time     DATETIME      NOT NULL,
  collector_id BIGINT,
  remark       VARCHAR(255),
  create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_payno (payment_no),
  KEY idx_bill (bill_id)
) ENGINE=InnoDB COMMENT='缴费记录';

-- ---------- 16. parking_space 车位 ----------
DROP TABLE IF EXISTS parking_space;
CREATE TABLE parking_space (
  id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
  community_id BIGINT        NOT NULL,
  space_no     VARCHAR(20)   NOT NULL,
  area_type    TINYINT       NOT NULL,
  use_type     TINYINT       NOT NULL,
  owner_id     BIGINT,
  plate_no     VARCHAR(20),
  monthly_fee  DECIMAL(10,2),
  start_date   DATE,
  end_date     DATE,
  status       TINYINT       NOT NULL DEFAULT 1,
  remark       VARCHAR(255),
  create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    VARCHAR(50),
  update_by    VARCHAR(50),
  deleted      TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_comm_spaceno (community_id, space_no)
) ENGINE=InnoDB COMMENT='车位';

-- ---------- 17. parking_record ----------
DROP TABLE IF EXISTS parking_record;
CREATE TABLE parking_record (
  id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
  space_id    BIGINT        NOT NULL,
  owner_id    BIGINT,
  plate_no    VARCHAR(20),
  action      VARCHAR(20),
  amount      DECIMAL(12,2),
  start_date  DATE,
  end_date    DATE,
  operator_id BIGINT,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_space (space_id)
) ENGINE=InnoDB COMMENT='车位操作记录';

-- ---------- 18. equipment 设备 ----------
DROP TABLE IF EXISTS equipment;
CREATE TABLE equipment (
  id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
  community_id    BIGINT       NOT NULL,
  category        TINYINT      NOT NULL,
  name            VARCHAR(50)  NOT NULL,
  code            VARCHAR(30)  NOT NULL,
  location        VARCHAR(100),
  model           VARCHAR(50),
  manufacturer    VARCHAR(100),
  install_date    DATE,
  warranty_date   DATE,
  online_status   TINYINT      NOT NULL DEFAULT 1,
  status          TINYINT      NOT NULL DEFAULT 1,
  last_check_date DATE,
  next_check_date DATE,
  remark          VARCHAR(255),
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by       VARCHAR(50),
  update_by       VARCHAR(50),
  deleted         TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY uk_comm_cat_code (community_id, category, code),
  KEY idx_status (status)
) ENGINE=InnoDB COMMENT='设备';

-- ---------- 19. equipment_check 巡检记录 ----------
DROP TABLE IF EXISTS equipment_check;
CREATE TABLE equipment_check (
  id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
  equipment_id BIGINT       NOT NULL,
  checker_id   BIGINT,
  check_time   DATETIME     NOT NULL,
  result       TINYINT      NOT NULL,
  issue_desc   VARCHAR(255),
  images       VARCHAR(500),
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_equipment (equipment_id)
) ENGINE=InnoDB COMMENT='设备巡检记录';
