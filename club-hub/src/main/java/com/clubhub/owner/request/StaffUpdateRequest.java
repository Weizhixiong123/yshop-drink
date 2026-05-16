package com.clubhub.owner.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffUpdateRequest {

    @NotBlank(message = "员工ID不能为空")
    private String id;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过 50 字")
    private String name;

    @Pattern(regexp = "^(staff|manager)$", message = "员工角色只能是 staff 或 manager")
    private String role;
}
