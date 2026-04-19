package com.clubhub.owner.response;

import com.clubhub.entity.OperationLog;
import lombok.Data;

import java.util.List;

@Data
public class OperationLogPageResponse {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<OperationLog> list;
}
