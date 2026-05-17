package com.clubhub.owner.request;

import lombok.Data;

@Data
public class OperationLogQueryRequest {
    private Long memberId;
    private String staffId;
    private String operationType;
    private String startTime;
    private String endTime;
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}
