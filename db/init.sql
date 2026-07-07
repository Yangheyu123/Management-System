-- ============================================
-- 1. 在 MySQL 中执行本脚本即可建库建表插数据
--    方式一：CMD 里  mysql -u root -p < init.sql
--    方式二：用 Navicat / DataGrip 打开本文件直接运行
-- ============================================

CREATE DATABASE IF NOT EXISTS demo DEFAULT CHARACTER SET utf8mb4;
USE demo;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id       INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50)  NOT NULL,
  age      INT,
  email    VARCHAR(100)
);

INSERT INTO user (username, age, email) VALUES
('张三', 20, 'zhangsan@qq.com'),
('李四', 22, 'lisi@qq.com'),
('王五', 21, 'wangwu@qq.com'),
('赵六', 23, 'zhaoliu@qq.com'),
('钱七', 20, 'qianqi@qq.com');

-- 验证一下
SELECT * FROM user;
