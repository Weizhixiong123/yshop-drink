package com.clubhub.staff.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberPackageConsumeRequest {
    @NotNull(message = "客户ID不能为空")
    private Long memberId;

    @NotNull(message = "套餐不能为空")
    private Integer packageAmount;
}
