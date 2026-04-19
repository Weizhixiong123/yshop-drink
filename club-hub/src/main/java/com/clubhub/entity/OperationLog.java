package com.clubhub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作员工ID */
    private Long staffId;

    /** 操作员工姓名 */
    private String staffName;

    /** 客户ID */
    private Long memberId;

    /** 客户姓名 */
    private String memberName;

    /** 操作类型: add_points/sub_points/add_wine/sub_wine/add_balance/sub_balance */
    private String operationType;

    /** 操作数值 */
    private BigDecimal operationValue;

    /** 变更前数值 */
    private BigDecimal beforeValue;

    /** 变更后数值 */
    private BigDecimal afterValue;

    private LocalDateTime createTime;
}
