package com.clubhub.owner.service;

import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.owner.request.MemberQueryRequest;
import com.clubhub.owner.request.MemberLevelUpdateRequest;
import com.clubhub.owner.request.OperationLogQueryRequest;
import com.clubhub.owner.request.StaffAddRequest;
import com.clubhub.owner.request.StaffUpdateRequest;

import java.util.List;

public interface OwnerService {

    // ========== 员工管理 ==========

    Result<?> addStaff(StaffAddRequest request);

    Result<?> listStaff();

    Result<?> updateStaff(StaffUpdateRequest request);

    Result<?> deleteStaff(String id);

    // ========== 操作日志 ==========

    Result<?> listLogs(OperationLogQueryRequest query);

    List<OperationLog> listLogsForExport(OperationLogQueryRequest query);

    // ========== 统计 ==========

    Result<?> todayStat();

    Result<?> accountStat(String date);

    Result<?> updateMemberLevel(MemberLevelUpdateRequest request);

    // ========== 客户资料 ==========

    Result<?> listMembers(MemberQueryRequest query);

    List<Member> listAllMembers();
}
