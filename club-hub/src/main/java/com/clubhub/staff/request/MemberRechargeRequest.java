package com.clubhub.staff.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRechargeRequest {
    @NotNull(message = "客户ID不能为空")
    private Long memberId;

    @NotNull(message = "充值档位不能为空")
    private Integer amount;
}
