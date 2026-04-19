package com.clubhub.staff.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberInfoUpdateRequest {

    @NotNull(message = "客户ID不能为空")
    private Long id;

    private String name;

    private String gender;

    private String phone;
}
