package com.clubhub.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffWxLoginRequest {

    @NotBlank(message = "微信手机号授权码不能为空")
    private String code;
}
