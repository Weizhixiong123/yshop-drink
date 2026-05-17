-- 创建数据库
CREATE DATABASE IF NOT EXISTS club_hub DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE club_hub;

-- 员工邀请表（店员）
CREATE TABLE IF NOT EXISTS staff (
    id          VARCHAR(32) PRIMARY KEY COMMENT '员工UUID',
    phone       VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    role        VARCHAR(20)  NOT NULL DEFAULT 'staff' COMMENT '角色: staff=店员, manager=店长',
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
    principal_balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '本金余额',
    bonus_balance     DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '赠送余额',
    level       VARCHAR(20)    NOT NULL DEFAULT 'normal' COMMENT '会员等级: normal/gold/platinum/black_gold/black_diamond',
    level_manual TINYINT       NOT NULL DEFAULT 0 COMMENT '是否店东手动锁定等级: 0=否, 1=是',
    remark      VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
    INDEX idx_member_create_id (create_time, id),
    INDEX idx_member_gender_create_id (gender, create_time, id),
    INDEX idx_member_name_create_id (name, create_time, id)
) COMMENT '会员表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id        VARCHAR(32)  NOT NULL COMMENT '操作员工ID',
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

-- 业务流水表：用于门店对账统计、码量统计、积分分类统计
CREATE TABLE IF NOT EXISTS business_record (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id          VARCHAR(32)   NOT NULL COMMENT '操作人ID',
    staff_name        VARCHAR(50)   NOT NULL COMMENT '操作人姓名',
    member_id         BIGINT        DEFAULT NULL COMMENT '会员ID，可为空',
    member_name       VARCHAR(50)   DEFAULT NULL COMMENT '会员姓名，可为空',
    record_type       VARCHAR(40)   NOT NULL COMMENT '流水类型',
    amount            DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '营收/扣款金额',
    principal_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本金金额',
    bonus_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '赠送金额',
    points_amount     INT           NOT NULL DEFAULT 0 COMMENT '积分数量',
    wine_quantity     INT           NOT NULL DEFAULT 0 COMMENT '酒水数量',
    chip_amount       INT           NOT NULL DEFAULT 0 COMMENT '码量，单位：千',
    package_code      VARCHAR(20)   DEFAULT NULL COMMENT '套餐/档位编码',
    remark            VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_type_time (record_type, create_time),
    INDEX idx_create_time (create_time),
    INDEX idx_member_id (member_id),
    INDEX idx_staff_id (staff_id)
) COMMENT '业务流水表';

-- 店东账号密码不入库，在 application.yml 的 owner.account 下配置。
