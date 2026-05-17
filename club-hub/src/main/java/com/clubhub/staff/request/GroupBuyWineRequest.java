package com.clubhub.staff.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupBuyWineRequest {
    private Long memberId;

    @NotNull(message = "酒水数量不能为空")
    private Integer wineQuantity;
}
