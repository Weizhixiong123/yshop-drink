package com.clubhub.staff.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberRegisterRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过 50 字")
    private String name;

    @Pattern(regexp = "^[MF]$", message = "性别只能是 M 或 F")
    private String gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Integer wine;

    private Integer points;

    private BigDecimal balance;

    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;
}
