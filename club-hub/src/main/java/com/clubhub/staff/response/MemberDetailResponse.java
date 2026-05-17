package com.clubhub.staff.response;

import com.clubhub.entity.BusinessRecord;
import com.clubhub.entity.OperationLog;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberDetailResponse {
    private Long id;
    private String name;
    private String gender;
    private String phone;
    private Integer wine;
    private Integer points;
    private BigDecimal balance;
    private BigDecimal principalBalance;
    private BigDecimal bonusBalance;
    private String level;
    private Boolean levelManual;
    private String remark;
    private List<BusinessRecord> businessRecords;
    private List<OperationLog> operationRecords;
}
