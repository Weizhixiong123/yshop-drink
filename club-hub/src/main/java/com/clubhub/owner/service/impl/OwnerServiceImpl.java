package com.clubhub.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clubhub.dto.Result;
import com.clubhub.entity.BusinessRecord;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.entity.Staff;
import com.clubhub.mapper.BusinessRecordMapper;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.OperationLogMapper;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.owner.request.MemberLevelUpdateRequest;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {
    private static final long DEFAULT_LOG_PAGE_SIZE = 10L;

    private final StaffMapper staffMapper;
    private final MemberMapper memberMapper;
    private final OperationLogMapper operationLogMapper;
    private final BusinessRecordMapper businessRecordMapper;

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
        staff.setRole(normalizeStaffRole(request.getRole()));
        staff.setStatus(1);
        staff.setDeleted(0);
        staffMapper.insert(staff);
        return Result.ok();
    }

    @Override
    public Result<?> listStaff() {
        List<Staff> list = staffMapper.selectList(
                new LambdaQueryWrapper<Staff>()
                        .in(Staff::getRole, "staff", "manager")
                        .orderByDesc(Staff::getCreateTime));
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
        staff.setRole(normalizeStaffRole(request.getRole()));
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
        Long pageSize = normalizeLogPageSize(query.getPageSize());
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

    private Long normalizeLogPageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1L) {
            return DEFAULT_LOG_PAGE_SIZE;
        }
        return Math.min(pageSize, DEFAULT_LOG_PAGE_SIZE);
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

    @Override
    public Result<?> accountStat(String date) {
        LocalDate statDate = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();
        List<BusinessRecord> records = businessRecordMapper.selectList(
                new LambdaQueryWrapper<BusinessRecord>()
                        .ge(BusinessRecord::getCreateTime, start)
                        .lt(BusinessRecord::getCreateTime, end)
                        .orderByDesc(BusinessRecord::getCreateTime));

        BigDecimal rechargeRevenue = sumAmount(records, "member_recharge", BusinessRecord::getPrincipalAmount);
        BigDecimal groupBuyRevenue = sumAmount(records, "group_buy_wine", BusinessRecord::getAmount);
        BigDecimal totalRevenue = rechargeRevenue.add(groupBuyRevenue);

        int packageChips = sumInt(records, "package_consume", BusinessRecord::getChipAmount);
        int pointsChips = sumInt(records, "points_exchange_chips", BusinessRecord::getChipAmount);
        int groupBuyChips = sumInt(records, "group_buy_wine", BusinessRecord::getChipAmount);
        int giftChips = sumInt(records, "gift_chips", BusinessRecord::getChipAmount);

        int fixedPoints = sumInt(records, "points_exchange_chips", BusinessRecord::getPointsAmount);
        int freePoints = operationLogMapper.selectList(
                        new LambdaQueryWrapper<OperationLog>()
                                .eq(OperationLog::getOperationType, "sub_points")
                                .ge(OperationLog::getCreateTime, start)
                                .lt(OperationLog::getCreateTime, end))
                .stream()
                .map(log -> log.getOperationValue() == null ? BigDecimal.ZERO : log.getOperationValue())
                .mapToInt(BigDecimal::intValue)
                .sum();

        Map<String, List<BusinessRecord>> details = records.stream()
                .collect(Collectors.groupingBy(BusinessRecord::getRecordType));

        return Result.ok(Map.of(
                "date", statDate.toString(),
                "revenue", Map.of(
                        "recharge", rechargeRevenue,
                        "groupBuy", groupBuyRevenue,
                        "total", totalRevenue
                ),
                "chips", Map.of(
                        "packageCash", packageChips,
                        "pointsExchange", pointsChips,
                        "groupBuy", groupBuyChips,
                        "gift", giftChips,
                        "total", packageChips + pointsChips + groupBuyChips + giftChips
                ),
                "points", Map.of(
                        "fixed", fixedPoints,
                        "free", freePoints,
                        "total", fixedPoints + freePoints
                ),
                "records", records,
                "details", details
        ));
    }

    @Override
    public Result<?> updateMemberLevel(MemberLevelUpdateRequest request) {
        Member member = memberMapper.selectById(request.getMemberId());
        if (member == null) {
            return Result.fail("客户不存在");
        }
        String level = normalizeMemberLevel(request.getLevel());
        Member update = new Member();
        update.setId(request.getMemberId());
        update.setLevel(level);
        update.setLevelManual(1);
        memberMapper.updateById(update);
        return Result.ok();
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

    private BigDecimal sumAmount(List<BusinessRecord> records, String type,
                                 java.util.function.Function<BusinessRecord, BigDecimal> getter) {
        return records.stream()
                .filter(record -> type.equals(record.getRecordType()))
                .map(getter)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int sumInt(List<BusinessRecord> records, String type,
                       java.util.function.Function<BusinessRecord, Integer> getter) {
        return records.stream()
                .filter(record -> type.equals(record.getRecordType()))
                .map(getter)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String normalizeMemberLevel(String level) {
        if ("gold".equals(level) || "platinum".equals(level) || "black_gold".equals(level)
                || "black_diamond".equals(level)) {
            return level;
        }
        return "normal";
    }

    private String normalizeStaffRole(String role) {
        if ("manager".equals(role)) {
            return "manager";
        }
        return "staff";
    }
}
