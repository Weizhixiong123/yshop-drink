package com.clubhub.customer.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerInfoResponse {
    private String name;
    private Integer wine;
    private Integer points;
    private BigDecimal balance;
    private String remark;
}
