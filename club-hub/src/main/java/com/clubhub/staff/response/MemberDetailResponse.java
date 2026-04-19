package com.clubhub.staff.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberDetailResponse {
    private Long id;
    private String name;
    private String gender;
    private String phone;
    private Integer wine;
    private Integer points;
    private BigDecimal balance;
    private String remark;
}
