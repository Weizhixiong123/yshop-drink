-- 创建数据库
CREATE DATABASE IF NOT EXISTS club_hub DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE club_hub;

-- 员工邀请表（店员）
CREATE TABLE IF NOT EXISTS staff (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    role        VARCHAR(20)  NOT NULL DEFAULT 'staff' COMMENT '角色: staff=店员',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除'
) COMMENT '员工邀请表';

-- 会员表
CREATE TABLE IF NOT EXISTS member (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)    NOT NULL COMMENT '姓名',
    gender      CHAR(1)        NOT NULL DEFAULT 'M' COMMENT '性别: M=男, F=女',
    phone       VARCHAR(20)    NOT NULL UNIQUE COMMENT '手机号',
    wine        INT            NOT NULL DEFAULT 0 COMMENT '存酒数量',
    points      INT            NOT NULL DEFAULT 0 COMMENT '积分',
    balance     DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '储值余额',
    remark      VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
    INDEX idx_phone_tail (phone)
) COMMENT '会员表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id        BIGINT       NOT NULL COMMENT '操作员工ID',
    staff_name      VARCHAR(50)  NOT NULL COMMENT '操作员工姓名',
    member_id       BIGINT       NOT NULL COMMENT '客户ID',
    member_name     VARCHAR(50)  NOT NULL COMMENT '客户姓名',
    operation_type  VARCHAR(30)  NOT NULL COMMENT '操作类型',
    operation_value DECIMAL(10, 2) NOT NULL COMMENT '操作数值',
    before_value    DECIMAL(10, 2) NOT NULL COMMENT '变更前数值',
    after_value     DECIMAL(10, 2) NOT NULL COMMENT '变更后数值',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_create_time (create_time),
    INDEX idx_member_id (member_id),
    INDEX idx_staff_id (staff_id)
) COMMENT '操作日志表';

-- 店东账号密码不入库，在 application.yml 的 owner.account 下配置。
