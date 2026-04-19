package com.clubhub.owner.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatResponse {
    private String statDate;
    private BigDecimal totalAddPoints;
    private BigDecimal totalSpecifiedSubPoints;
    private BigDecimal chipValue;
}
