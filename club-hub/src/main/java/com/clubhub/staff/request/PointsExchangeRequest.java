package com.clubhub.staff.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsExchangeRequest {
    @NotNull(message = "客户ID不能为空")
    private Long memberId;

    @NotNull(message = "套餐不能为空")
    private Integer packageAmount;

    @NotNull(message = "兑换倍数不能为空")
    private Integer multiplier;
}
