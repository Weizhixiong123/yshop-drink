package com.clubhub.customer.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerInfoResponse {
    private Long id;
    private String name;
    private Integer wine;
    private Integer points;
    private BigDecimal balance;
    private BigDecimal principalBalance;
    private BigDecimal bonusBalance;
    private String level;
    private String remark;
}
