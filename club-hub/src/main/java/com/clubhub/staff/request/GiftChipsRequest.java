package com.clubhub.staff.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GiftChipsRequest {
    @NotNull(message = "赠送码量不能为空")
    private Integer chipAmount;

    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;
}
