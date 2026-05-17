USE club_hub;

ALTER TABLE member
    ADD COLUMN principal_balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '本金余额' AFTER balance,
    ADD COLUMN bonus_balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '赠送余额' AFTER principal_balance,
    ADD COLUMN level VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '会员等级: normal/gold/platinum/black_gold/black_diamond' AFTER bonus_balance,
    ADD COLUMN level_manual TINYINT NOT NULL DEFAULT 0 COMMENT '是否店东手动锁定等级: 0=否, 1=是' AFTER level;

UPDATE member
SET principal_balance = balance,
    bonus_balance = 0.00,
    level = 'normal',
    level_manual = 0
WHERE principal_balance = 0.00
  AND bonus_balance = 0.00;

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
