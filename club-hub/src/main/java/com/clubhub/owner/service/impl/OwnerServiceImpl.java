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
import com.clubhub.owner.request.MemberQueryRequest;
import com.clubhub.owner.request.OperationLogQueryRequest;
import com.clubhub.owner.request.StaffAddRequest;
import com.clubhub.owner.request.StaffUpdateRequest;
import com.clubhub.owner.response.MemberPageResponse;
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
        String phone = request.getPhone().trim();
        Staff exist = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>().eq(Staff::getPhone, phone));
        if (exist != null) {
            return Result.fail("手机号已被设置为员工");
        }
        Staff staff = new Staff();
        staff.setPhone(phone);
        staff.setName(request.getName().trim());
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
    public Result<?> updateStaff(StaffUpdateRequest request) {
        Staff exist = staffMapper.selectById(request.getId());
        if (exist == null) {
            return Result.fail("员工不存在");
        }

        String phone = request.getPhone().trim();
        Long count = staffMapper.selectCount(
                new LambdaQueryWrapper<Staff>()
                        .eq(Staff::getPhone, phone)
                        .ne(Staff::getId, request.getId()));
        if (count > 0) {
            return Result.fail("手机号已被其他员工使用");
        }

        Staff staff = new Staff();
        staff.setId(request.getId());
        staff.setPhone(phone);
        staff.setName(request.getName().trim());
        staffMapper.updateById(staff);
        return Result.ok();
    }

    @Override
    public Result<?> deleteStaff(String id) {
        staffMapper.deleteById(id);
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
    public Result<?> listMembers(MemberQueryRequest query) {
        LambdaQueryWrapper<Member> wrapper = buildMemberQuery(query);
        Long pageNum = query.getPageNum() == null ? 1L : query.getPageNum();
        Long pageSize = normalizePageSize(query.getPageSize());
        Page<Member> page = memberMapper.selectPage(Page.of(pageNum, pageSize), wrapper);

        MemberPageResponse resp = new MemberPageResponse();
        resp.setTotal(page.getTotal());
        resp.setPageNum(page.getCurrent());
        resp.setPageSize(page.getSize());
        resp.setList(page.getRecords());
        return Result.ok(resp);
    }

    @Override
    public List<Member> listAllMembers() {
        return memberMapper.selectList(new LambdaQueryWrapper<Member>().orderByDesc(Member::getCreateTime));
    }

    private LambdaQueryWrapper<Member> buildMemberQuery(MemberQueryRequest q) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (q.getId() != null) wrapper.eq(Member::getId, q.getId());
        if (q.getName() != null && !q.getName().isBlank()) {
            wrapper.likeRight(Member::getName, q.getName().trim());
        }
        if (q.getGender() != null && !q.getGender().isBlank()) {
            wrapper.eq(Member::getGender, q.getGender().trim());
        }
        if (q.getPhone() != null && !q.getPhone().isBlank()) {
            String phone = q.getPhone().trim();
            if (phone.matches("^1[3-9]\\d{9}$")) {
                wrapper.eq(Member::getPhone, phone);
            } else {
                wrapper.likeRight(Member::getPhone, phone);
            }
        }
        wrapper.orderByDesc(Member::getCreateTime, Member::getId);
        return wrapper;
    }

    private Long normalizePageSize(Long pageSize) {
        if (pageSize == null) {
            return 20L;
        }
        return Math.min(Math.max(pageSize, 1L), 100L);
    }
}
