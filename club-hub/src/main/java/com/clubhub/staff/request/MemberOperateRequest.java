package com.clubhub.staff.request;

import com.clubhub.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberOperateRequest {

    @NotNull(message = "客户ID不能为空")
    private Long memberId;

    @NotNull(message = "操作类型不能为空")
    private OperationType type;

    @NotNull(message = "操作值不能为空")
    private BigDecimal value;
}
