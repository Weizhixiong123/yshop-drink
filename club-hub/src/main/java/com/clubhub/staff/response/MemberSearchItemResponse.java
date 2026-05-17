package com.clubhub.staff.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberSearchItemResponse {
    private Long id;
    private String name;
    private String phone;
    private Integer wine;
    private Integer points;
    private BigDecimal balance;
    private BigDecimal principalBalance;
    private BigDecimal bonusBalance;
    private String level;
    private String remark;
}
