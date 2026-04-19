package com.clubhub.owner.service;

import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.owner.request.OperationLogQueryRequest;
import com.clubhub.owner.request.StaffAddRequest;

import java.util.List;

public interface OwnerService {

    // ========== 员工管理 ==========

    Result<?> addStaff(StaffAddRequest request);

    Result<?> listStaff();

    Result<?> updateStaffStatus(Long id, Integer status);

    Result<?> deleteStaff(Long id);

    // ========== 操作日志 ==========

    Result<?> listLogs(OperationLogQueryRequest query);

    List<OperationLog> listLogsForExport(OperationLogQueryRequest query);

    // ========== 统计 ==========

    Result<?> todayStat();

    // ========== 客户资料 ==========

    List<Member> listAllMembers();
}
