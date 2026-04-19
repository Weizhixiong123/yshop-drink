package com.clubhub.staff.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffPasswordChangeRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
