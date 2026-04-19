package com.clubhub.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.entity.Staff;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.OperationLogMapper;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.owner.request.OperationLogQueryRequest;
import com.clubhub.owner.request.StaffAddRequest;
import com.clubhub.owner.request.StaffPasswordResetRequest;
import com.clubhub.owner.response.OperationLogPageResponse;
import com.clubhub.owner.response.StaffResponse;
import com.clubhub.owner.response.TodayStatResponse;
import com.clubhub.owner.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final StaffMapper staffMapper;
    private final MemberMapper memberMapper;
    private final OperationLogMapper operationLogMapper;

    // ========== 员工管理 ==========

    @Override
    public Result<?> addStaff(StaffAddRequest request) {
        Staff exist = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, request.getUsername()));
        if (exist != null) {
            return Result.fail("账号已存在");
        }
        Staff staff = new Staff();
        BeanUtils.copyProperties(request, staff);
        staff.setRole("staff");
        staff.setStatus(1);
        staff.setDeleted(0);
        staffMapper.insert(staff);
        return Result.ok();
    }

    @Override
    public Result<?> listStaff() {
        List<Staff> list = staffMapper.selectList(
                new LambdaQueryWrapper<Staff>().eq(Staff::getRole, "staff").orderByDesc(Staff::getCreateTime));
        List<StaffResponse> result = list.stream().map(s -> {
            StaffResponse r = new StaffResponse();
            BeanUtils.copyProperties(s, r);
            return r;
        }).toList();
        return Result.ok(result);
    }

    @Override
    public Result<?> updateStaffStatus(Long id, Integer status) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setStatus(status);
        staffMapper.updateById(staff);
        return Result.ok();
    }

    @Override
    public Result<?> deleteStaff(Long id) {
        staffMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    public Result<?> resetStaffPassword(StaffPasswordResetRequest request) {
        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码至少 6 位");
        }
        Staff staff = staffMapper.selectById(request.getId());
        if (staff == null) {
            return Result.fail("员工不存在");
        }
        if ("owner".equals(staff.getRole())) {
            return Result.fail("不能通过此接口重置店东密码");
        }
        Staff update = new Staff();
        update.setId(request.getId());
        update.setPassword(newPassword);
        staffMapper.updateById(update);
        return Result.ok();
    }

    // ========== 操作日志 ==========

    @Override
    public Result<?> listLogs(OperationLogQueryRequest query) {
        LambdaQueryWrapper<OperationLog> wrapper = buildLogQuery(query);
        Long pageNum = query.getPageNum() == null ? 1L : query.getPageNum();
        Long pageSize = query.getPageSize() == null ? 20L : query.getPageSize();
        Page<OperationLog> page = operationLogMapper.selectPage(Page.of(pageNum, pageSize), wrapper);

        OperationLogPageResponse resp = new OperationLogPageResponse();
        resp.setTotal(page.getTotal());
        resp.setPageNum(page.getCurrent());
        resp.setPageSize(page.getSize());
        resp.setList(page.getRecords());
        return Result.ok(resp);
    }

    @Override
    public List<OperationLog> listLogsForExport(OperationLogQueryRequest query) {
        return operationLogMapper.selectList(buildLogQuery(query));
    }

    private LambdaQueryWrapper<OperationLog> buildLogQuery(OperationLogQueryRequest q) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (q.getMemberId() != null) wrapper.eq(OperationLog::getMemberId, q.getMemberId());
        if (q.getStaffId() != null) wrapper.eq(OperationLog::getStaffId, q.getStaffId());
        if (q.getOperationType() != null && !q.getOperationType().isBlank()) {
            wrapper.eq(OperationLog::getOperationType, q.getOperationType());
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (q.getStartTime() != null && !q.getStartTime().isBlank()) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(q.getStartTime(), fmt));
        }
        if (q.getEndTime() != null && !q.getEndTime().isBlank()) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(q.getEndTime(), fmt));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return wrapper;
    }

    // ========== 统计 ==========

    @Override
    public Result<?> todayStat() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;

        if (now.toLocalTime().isBefore(LocalTime.NOON)) {
            start = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.NOON);
            end = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
        } else {
            start = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
            end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON);
        }

        BigDecimal totalAddPoints = operationLogMapper.sumAddPoints(start, end);
        BigDecimal totalSpecifiedSubPoints = operationLogMapper.sumSpecifiedSubPoints(start, end);
        BigDecimal chipValue = totalSpecifiedSubPoints.divide(BigDecimal.valueOf(2));

        String statDate = start.toLocalDate() + " 12:00 ~ " + end.toLocalDate() + " 12:00";
        return Result.ok(new TodayStatResponse(statDate, totalAddPoints, totalSpecifiedSubPoints, chipValue));
    }

    // ========== 客户资料 ==========

    @Override
    public List<Member> listAllMembers() {
        return memberMapper.selectList(new LambdaQueryWrapper<Member>().orderByDesc(Member::getCreateTime));
    }
}
