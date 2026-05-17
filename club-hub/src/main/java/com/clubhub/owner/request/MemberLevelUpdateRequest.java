package com.clubhub.owner.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberLevelUpdateRequest {
    @NotNull(message = "客户ID不能为空")
    private Long memberId;

    @NotNull(message = "会员等级不能为空")
    private String level;
}
