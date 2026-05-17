package com.clubhub.staff.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.dto.Result;
import com.clubhub.entity.BusinessRecord;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.entity.Staff;
import com.clubhub.enums.OperationType;
import com.clubhub.mapper.BusinessRecordMapper;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.OperationLogMapper;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.staff.request.GroupBuyWineRequest;
import com.clubhub.staff.request.GiftChipsRequest;
import com.clubhub.staff.request.MemberInfoUpdateRequest;
import com.clubhub.staff.request.MemberOperateRequest;
import com.clubhub.staff.request.MemberPackageConsumeRequest;
import com.clubhub.staff.request.MemberRechargeRequest;
import com.clubhub.staff.request.MemberRegisterRequest;
import com.clubhub.staff.request.MemberRemarkUpdateRequest;
import com.clubhub.staff.request.PointsExchangeRequest;
import com.clubhub.staff.response.MemberDetailResponse;
import com.clubhub.staff.response.MemberOperateResponse;
import com.clubhub.staff.response.MemberSearchItemResponse;
import com.clubhub.staff.service.StaffService;
import com.clubhub.websocket.StaffDataUpdateNotifier;
import com.clubhub.websocket.UserDataUpdateNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffMapper staffMapper;
    private final MemberMapper memberMapper;
    private final OperationLogMapper operationLogMapper;
    private final BusinessRecordMapper businessRecordMapper;
    private final UserDataUpdateNotifier userDataUpdateNotifier;
    private final StaffDataUpdateNotifier staffDataUpdateNotifier;

    private static final Map<Integer, Integer> RECHARGE_BONUS = Map.of(
            500, 168,
            1000, 388,
            2000, 888,
            5000, 3888
    );

    private static final Map<Integer, PackagePlan> PACKAGE_PLANS = Map.of(
            128, new PackagePlan(128, 4, 10),
            228, new PackagePlan(228, 8, 20),
            328, new PackagePlan(328, 12, 30),
            498, new PackagePlan(498, 20, 60)
    );

    private static final Map<Integer, GroupBuyPlan> GROUP_BUY_PLANS = Map.of(
            2, new GroupBuyPlan(50, 5),
            4, new GroupBuyPlan(100, 10),
            8, new GroupBuyPlan(200, 20)
    );

    @Value("${owner.account.name:店东}")
    private String ownerName;

    @Override
    public Result<?> registerMember(MemberRegisterRequest request) {
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, request.getPhone()));
        if (count > 0) {
            return Result.fail("该手机号已开卡，请勿重复开卡");
        }
        Member member = new Member();
        BeanUtils.copyProperties(request, member);
        if (member.getWine() == null) member.setWine(0);
        if (member.getPoints() == null) member.setPoints(0);
        if (member.getBalance() == null) member.setBalance(BigDecimal.ZERO);
        if (member.getPrincipalBalance() == null) member.setPrincipalBalance(member.getBalance());
        if (member.getBonusBalance() == null) member.setBonusBalance(BigDecimal.ZERO);
        if (member.getLevel() == null || member.getLevel().isBlank()) member.setLevel("normal");
        if (member.getLevelManual() == null) member.setLevelManual(0);
        member.setDeleted(0);
        memberMapper.insert(member);
        return Result.ok();
    }

    @Override
    public Result<?> searchMembers(String keyword, String tail, Integer limit, String role) {
        boolean usingTail = (keyword == null || keyword.isBlank()) && tail != null && !tail.isBlank();
        String query = normalizeSearchKeyword(keyword, tail);
        if (query == null) {
            return Result.fail("请输入搜索关键词");
        }

        boolean phoneTailSearch = query.matches("\\d+");
        if (usingTail && !phoneTailSearch) {
            return Result.fail("手机号尾号只能输入数字");
        }
        if (phoneTailSearch && query.length() > 11) {
            return Result.fail("搜索数字不能超过 11 位");
        }

        Long memberId = parseMemberId(query);
        int searchLimit = normalizeSearchLimit(limit);

        List<Member> list = memberMapper.selectList(
                new LambdaQueryWrapper<Member>()
                        .and(wrapper -> {
                            if (memberId != null) {
                                wrapper.eq(Member::getId, memberId).or();
                            }
                            wrapper.like(Member::getName, query)
                                    .or()
                                    .likeLeft(Member::getPhone, query);
                        })
                        .orderByDesc(Member::getCreateTime, Member::getId)
                        .last("LIMIT " + searchLimit));

        if (list.isEmpty()) {
            return Result.ok(List.of());
        }

        boolean shouldShowFullPhone = ("owner".equals(role) || "manager".equals(role))
                || (phoneTailSearch && hasDuplicatePhoneTail(list, query));

        List<MemberSearchItemResponse> result = list.stream().map(m -> {
            MemberSearchItemResponse item = new MemberSearchItemResponse();
            item.setId(m.getId());
            item.setName(m.getName());
            item.setWine(m.getWine());
            item.setPoints(m.getPoints());
            item.setBalance(m.getBalance());
            item.setPrincipalBalance(safe(m.getPrincipalBalance()));
            item.setBonusBalance(safe(m.getBonusBalance()));
            item.setLevel(normalizeLevel(m.getLevel()));
            item.setRemark(m.getRemark());

            item.setPhone(shouldShowFullPhone ? m.getPhone() : maskPhone(m.getPhone()));
            return item;
        }).toList();

        return Result.ok(result);
    }

    private String normalizeSearchKeyword(String keyword, String tail) {
        if (keyword != null && !keyword.isBlank()) {
            return keyword.trim();
        }
        if (tail != null && !tail.isBlank()) {
            return tail.trim();
        }
        return null;
    }

    private int normalizeSearchLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return Math.min(Math.max(limit, 1), 50);
    }

    private boolean hasDuplicatePhoneTail(List<Member> list, String tail) {
        long count = list.stream()
                .filter(member -> member.getPhone() != null && member.getPhone().endsWith(tail))
                .count();
        return count > 1;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return phone;
        }
        return "****" + phone.substring(phone.length() - 4);
    }

    private Long parseMemberId(String keyword) {
        if (!keyword.matches("\\d+")) {
            return null;
        }
        try {
            return Long.parseLong(keyword);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public Result<?> memberDetail(Long id, String role) {
        Member m = memberMapper.selectById(id);
        if (m == null) {
            return Result.fail("客户不存在");
        }

        MemberDetailResponse detail = new MemberDetailResponse();
        detail.setId(m.getId());
        detail.setName(m.getName());
        detail.setGender(m.getGender());
        detail.setWine(m.getWine());
        detail.setPoints(m.getPoints());
        detail.setBalance(m.getBalance());
        detail.setPrincipalBalance(safe(m.getPrincipalBalance()));
        detail.setBonusBalance(safe(m.getBonusBalance()));
        detail.setLevel(normalizeLevel(m.getLevel()));
        detail.setLevelManual(Integer.valueOf(1).equals(m.getLevelManual()));
        detail.setRemark(m.getRemark());
        detail.setBusinessRecords(businessRecordMapper.selectList(
                new LambdaQueryWrapper<BusinessRecord>()
                        .eq(BusinessRecord::getMemberId, id)
                        .ne(BusinessRecord::getRecordType, "group_buy_wine")
                        .orderByDesc(BusinessRecord::getCreateTime)
                        .last("LIMIT 80")));
        detail.setOperationRecords(operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>()
                        .eq(OperationLog::getMemberId, id)
                        .orderByDesc(OperationLog::getCreateTime)
                        .last("LIMIT 80")));

        if ("owner".equals(role) || "manager".equals(role)) {
            detail.setPhone(m.getPhone());
        } else {
            String tail = m.getPhone().substring(m.getPhone().length() - 4);
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>().likeLeft(Member::getPhone, tail));
            if (count > 1) {
                detail.setPhone(m.getPhone());
            } else {
                detail.setPhone("****" + tail);
            }
        }

        return Result.ok(detail);
    }

    @Override
    @Transactional
    public Result<?> operate(MemberOperateRequest request, String staffId, String role) {
        BigDecimal value = request.getValue();
        OperationType type = request.getType();
        Long memberId = request.getMemberId();

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail("操作值必须大于 0");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return Result.fail("客户不存在");
        }
        ensureMemberDefaults(member);
        if (type == OperationType.ADD_BALANCE && !canAddBalance(role)) {
            return Result.fail("只有店长可以加储值");
        }

        BigDecimal beforeValue;
        BigDecimal afterValue;

        switch (type) {
            case ADD_POINTS -> {
                beforeValue = BigDecimal.valueOf(member.getPoints());
                member.setPoints(member.getPoints() + value.intValue());
                afterValue = BigDecimal.valueOf(member.getPoints());
            }
            case SUB_POINTS -> {
                beforeValue = BigDecimal.valueOf(member.getPoints());
                if (member.getPoints() < value.intValue()) {
                    return Result.fail("积分不足");
                }
                member.setPoints(member.getPoints() - value.intValue());
                afterValue = BigDecimal.valueOf(member.getPoints());
            }
            case ADD_WINE -> {
                beforeValue = BigDecimal.valueOf(member.getWine());
                member.setWine(member.getWine() + value.intValue());
                afterValue = BigDecimal.valueOf(member.getWine());
            }
            case SUB_WINE -> {
                beforeValue = BigDecimal.valueOf(member.getWine());
                if (member.getWine() < value.intValue()) {
                    return Result.fail("存酒不足");
                }
                member.setWine(member.getWine() - value.intValue());
                afterValue = BigDecimal.valueOf(member.getWine());
            }
            case ADD_BALANCE -> {
                beforeValue = member.getBalance();
                member.setPrincipalBalance(safe(member.getPrincipalBalance()).add(value));
                syncTotalBalance(member);
                afterValue = member.getBalance();
            }
            case SUB_BALANCE -> {
                beforeValue = member.getBalance();
                DeductResult deduct = deductBalance(member, value);
                if (deduct.shortfall().compareTo(BigDecimal.ZERO) > 0) {
                    return Result.fail("储值余额不足");
                }
                syncTotalBalance(member);
                afterValue = member.getBalance();
            }
            default -> {
                return Result.fail("未知操作类型");
            }
        }

        memberMapper.updateById(member);

        OperationLog log = new OperationLog();
        log.setStaffId(staffId);
        log.setStaffName(getNameById(staffId));
        log.setMemberId(memberId);
        log.setMemberName(member.getName());
        log.setOperationType(type.getCode());
        log.setOperationValue(value);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);

        if (type == OperationType.ADD_POINTS
                || type == OperationType.SUB_POINTS
                || type == OperationType.ADD_WINE
                || type == OperationType.SUB_WINE
                || type == OperationType.ADD_BALANCE
                || type == OperationType.SUB_BALANCE) {
            userDataUpdateNotifier.notifyAfterCommit(memberId);
            staffDataUpdateNotifier.notifyAfterCommit(memberId);
        }

        return Result.ok(new MemberOperateResponse(beforeValue, afterValue));
    }

    @Override
    @Transactional
    public Result<?> recharge(MemberRechargeRequest request, String staffId, String role) {
        if (!"manager".equals(role)) {
            return Result.fail("只有店长可以进行储值充值");
        }
        Integer bonus = RECHARGE_BONUS.get(request.getAmount());
        if (bonus == null) {
            return Result.fail("请选择固定充值档位");
        }
        Member member = memberMapper.selectById(request.getMemberId());
        if (member == null) {
            return Result.fail("客户不存在");
        }
        ensureMemberDefaults(member);
        BigDecimal principal = BigDecimal.valueOf(request.getAmount());
        BigDecimal bonusValue = BigDecimal.valueOf(bonus);
        member.setPrincipalBalance(member.getPrincipalBalance().add(principal));
        member.setBonusBalance(member.getBonusBalance().add(bonusValue));
        syncTotalBalance(member);
        upgradeLevelAfterRecharge(member, principal);
        memberMapper.updateById(member);

        insertBusinessRecord(staffId, member, "member_recharge", principal, principal, bonusValue,
                0, 0, 0, String.valueOf(request.getAmount()), "充" + request.getAmount() + "送" + bonus);
        notifyMemberChanged(member.getId());
        return Result.ok(Map.of(
                "principalBalance", member.getPrincipalBalance(),
                "bonusBalance", member.getBonusBalance(),
                "balance", member.getBalance(),
                "level", member.getLevel()
        ));
    }

    @Override
    @Transactional
    public Result<?> consumePackage(MemberPackageConsumeRequest request, String staffId, String role) {
        PackagePlan plan = PACKAGE_PLANS.get(request.getPackageAmount());
        if (plan == null) {
            return Result.fail("请选择固定消费套餐");
        }
        Member member = memberMapper.selectById(request.getMemberId());
        if (member == null) {
            return Result.fail("客户不存在");
        }
        ensureMemberDefaults(member);
        DeductResult deduct = deductBalance(member, BigDecimal.valueOf(plan.amount()));
        if (deduct.shortfall().compareTo(BigDecimal.ZERO) > 0) {
            return Result.fail("储值余额不足，套餐消费未执行");
        }
        member.setWine(member.getWine() + plan.wine());
        syncTotalBalance(member);
        memberMapper.updateById(member);

        insertBusinessRecord(staffId, member, "package_consume", BigDecimal.valueOf(plan.amount()),
                deduct.principal(), deduct.bonus(), 0, plan.wine(), plan.chips(),
                String.valueOf(plan.amount()), null);
        notifyMemberChanged(member.getId());
        return Result.ok(Map.of(
                "principalBalance", member.getPrincipalBalance(),
                "bonusBalance", member.getBonusBalance(),
                "balance", member.getBalance(),
                "wine", member.getWine()
        ));
    }

    @Override
    @Transactional
    public Result<?> groupBuyWine(GroupBuyWineRequest request, String staffId) {
        GroupBuyPlan plan = GROUP_BUY_PLANS.get(request.getWineQuantity());
        if (plan == null) {
            return Result.fail("团购酒水只支持加2瓶、4瓶、8瓶");
        }
        Member member = request.getMemberId() == null ? null : memberMapper.selectById(request.getMemberId());
        if (request.getMemberId() != null && member == null) {
            return Result.fail("客户不存在");
        }
        if (member != null) {
            ensureMemberDefaults(member);
            member.setWine(member.getWine() + request.getWineQuantity());
            memberMapper.updateById(member);
            notifyMemberChanged(member.getId());
        }
        insertBusinessRecord(staffId, null, "group_buy_wine", BigDecimal.valueOf(plan.revenue()),
                BigDecimal.ZERO, BigDecimal.ZERO, 0, request.getWineQuantity(), plan.chips(),
                String.valueOf(request.getWineQuantity()), "线上团购散客订单");
        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> exchangePoints(PointsExchangeRequest request, String staffId, String role) {
        Member member = memberMapper.selectById(request.getMemberId());
        if (member == null) {
            return Result.fail("客户不存在");
        }
        ensureMemberDefaults(member);
        if (!canUsePointsExchange(member.getLevel())) {
            return Result.fail("仅黄金、白金、黑金会员可使用积分兑筹码");
        }
        if (!allowedMultiplier(request.getMultiplier())) {
            return Result.fail("当前时段不可使用该兑换档位");
        }
        PackagePlan plan = PACKAGE_PLANS.get(request.getPackageAmount());
        if (plan == null) {
            return Result.fail("请选择固定套餐");
        }
        int points = resolveExchangePoints(request.getPackageAmount(), request.getMultiplier());
        if (member.getPoints() < points) {
            return Result.fail("积分不足");
        }
        member.setPoints(member.getPoints() - points);
        memberMapper.updateById(member);

        int chips = plan.chips() * request.getMultiplier();
        insertBusinessRecord(staffId, member, "points_exchange_chips", BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, points, 0, chips,
                request.getPackageAmount() + "_ADD_" + request.getMultiplier(), "固定规则扣积分");
        notifyMemberChanged(member.getId());
        return Result.ok(Map.of("points", member.getPoints(), "deductedPoints", points, "chips", chips));
    }

    @Override
    @Transactional
    public Result<?> giftChips(GiftChipsRequest request, String staffId, String role) {
        if (!"manager".equals(role)) {
            return Result.fail("只有店长可以登记赠送码量");
        }
        if (request.getChipAmount() == null || request.getChipAmount() <= 0) {
            return Result.fail("赠送码量必须大于0");
        }
        BusinessRecord record = baseBusinessRecord(staffId, null, "gift_chips");
        record.setChipAmount(request.getChipAmount());
        record.setRemark(request.getRemark());
        businessRecordMapper.insert(record);
        return Result.ok();
    }

    @Override
    public Result<?> updateMemberRemark(MemberRemarkUpdateRequest request) {
        if (memberMapper.selectById(request.getId()) == null) {
            return Result.fail("客户不存在");
        }
        Member member = new Member();
        member.setId(request.getId());
        member.setRemark(request.getRemark());
        memberMapper.updateById(member);
        userDataUpdateNotifier.notifyAfterCommit(request.getId());
        staffDataUpdateNotifier.notifyAfterCommit(request.getId());
        return Result.ok();
    }

    @Override
    public Result<?> updateMemberInfo(MemberInfoUpdateRequest request) {
        Member exist = memberMapper.selectById(request.getId());
        if (exist == null) {
            return Result.fail("客户不存在");
        }
        Member update = new Member();
        update.setId(request.getId());

        String name = request.getName();
        String gender = request.getGender();
        String phone = request.getPhone();

        if (name != null && !name.isBlank()) {
            update.setName(name.trim());
        }
        if (gender != null && ("M".equals(gender) || "F".equals(gender))) {
            update.setGender(gender);
        }
        if (phone != null && !phone.isBlank() && !phone.equals(exist.getPhone())) {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return Result.fail("手机号格式不正确");
            }
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getPhone, phone)
                            .ne(Member::getId, request.getId()));
            if (count > 0) {
                return Result.fail("该手机号已被其他会员使用");
            }
            update.setPhone(phone);
        }
        memberMapper.updateById(update);
        userDataUpdateNotifier.notifyAfterCommit(request.getId());
        staffDataUpdateNotifier.notifyAfterCommit(request.getId());
        return Result.ok();
    }

    @Override
    public String getNameById(String staffId) {
        if ("owner".equals(staffId)) {
            return ownerName == null || ownerName.isBlank() ? "店东" : ownerName.trim();
        }
        Staff staff = staffMapper.selectById(staffId);
        return staff != null ? staff.getName() : "未知员工";
    }

    @Override
    public Staff getById(String staffId) {
        return staffMapper.selectById(staffId);
    }

    private boolean canAddBalance(String role) {
        return "manager".equals(role);
    }

    private void ensureMemberDefaults(Member member) {
        if (member.getWine() == null) member.setWine(0);
        if (member.getPoints() == null) member.setPoints(0);
        if (member.getBalance() == null) member.setBalance(BigDecimal.ZERO);
        if (member.getPrincipalBalance() == null) member.setPrincipalBalance(member.getBalance());
        if (member.getBonusBalance() == null) member.setBonusBalance(BigDecimal.ZERO);
        if (member.getLevel() == null || member.getLevel().isBlank()) member.setLevel("normal");
        if (member.getLevelManual() == null) member.setLevelManual(0);
        syncTotalBalance(member);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void syncTotalBalance(Member member) {
        member.setBalance(safe(member.getPrincipalBalance()).add(safe(member.getBonusBalance())));
    }

    private DeductResult deductBalance(Member member, BigDecimal amount) {
        BigDecimal principalBefore = safe(member.getPrincipalBalance());
        BigDecimal bonusBefore = safe(member.getBonusBalance());
        BigDecimal principalDeduct = principalBefore.min(amount);
        BigDecimal remain = amount.subtract(principalDeduct);
        BigDecimal bonusDeduct = bonusBefore.min(remain);
        BigDecimal shortfall = remain.subtract(bonusDeduct);
        member.setPrincipalBalance(principalBefore.subtract(principalDeduct));
        member.setBonusBalance(bonusBefore.subtract(bonusDeduct));
        return new DeductResult(principalDeduct, bonusDeduct, shortfall);
    }

    private void upgradeLevelAfterRecharge(Member member, BigDecimal principal) {
        if (Integer.valueOf(1).equals(member.getLevelManual())) {
            return;
        }
        String current = normalizeLevel(member.getLevel());
        BigDecimal monthTotal = businessRecordMapper.selectList(
                        new LambdaQueryWrapper<BusinessRecord>()
                                .eq(BusinessRecord::getMemberId, member.getId())
                                .eq(BusinessRecord::getRecordType, "member_recharge")
                                .ge(BusinessRecord::getCreateTime, YearMonth.now().atDay(1).atStartOfDay())
                                .lt(BusinessRecord::getCreateTime, YearMonth.now().plusMonths(1).atDay(1).atStartOfDay()))
                .stream()
                .map(BusinessRecord::getPrincipalAmount)
                .reduce(principal, BigDecimal::add);

        String target = current;
        if (monthTotal.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            target = "black_gold";
        } else if (principal.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            target = "platinum";
        } else if (principal.compareTo(BigDecimal.valueOf(2000)) >= 0
                || monthTotal.compareTo(BigDecimal.valueOf(4000)) >= 0) {
            target = "gold";
        }
        if (levelRank(target) > levelRank(current)) {
            member.setLevel(target);
        }
    }

    private int levelRank(String level) {
        return switch (normalizeLevel(level)) {
            case "gold" -> 1;
            case "platinum" -> 2;
            case "black_gold" -> 3;
            case "black_diamond" -> 4;
            default -> 0;
        };
    }

    private String normalizeLevel(String level) {
        if ("gold".equals(level) || "platinum".equals(level) || "black_gold".equals(level)
                || "black_diamond".equals(level)) {
            return level;
        }
        return "normal";
    }

    private boolean canUsePointsExchange(String level) {
        return "gold".equals(level) || "platinum".equals(level) || "black_gold".equals(level);
    }

    private boolean allowedMultiplier(Integer multiplier) {
        if (multiplier == null) return false;
        LocalTime now = LocalTime.now();
        if (!List.of(1, 2, 4).contains(multiplier)) return false;
        if (!now.isBefore(LocalTime.NOON)) return multiplier == 1;
        if (now.isBefore(LocalTime.of(3, 0))) return multiplier == 1 || multiplier == 2;
        return true;
    }

    private int resolveExchangePoints(Integer packageAmount, Integer multiplier) {
        if (packageAmount == 328 && multiplier == 4) {
            return 3200;
        }
        int base = switch (packageAmount) {
            case 128 -> 200;
            case 228 -> 400;
            case 328 -> 600;
            case 498 -> 1200;
            default -> throw new IllegalArgumentException("unknown package");
        };
        return base * multiplier;
    }

    private void insertBusinessRecord(String staffId, Member member, String recordType, BigDecimal amount,
                                      BigDecimal principalAmount, BigDecimal bonusAmount, Integer points,
                                      Integer wine, Integer chips, String packageCode, String remark) {
        BusinessRecord record = baseBusinessRecord(staffId, member, recordType);
        record.setAmount(amount);
        record.setPrincipalAmount(principalAmount);
        record.setBonusAmount(bonusAmount);
        record.setPointsAmount(points);
        record.setWineQuantity(wine);
        record.setChipAmount(chips);
        record.setPackageCode(packageCode);
        record.setRemark(remark);
        businessRecordMapper.insert(record);
    }

    private BusinessRecord baseBusinessRecord(String staffId, Member member, String recordType) {
        BusinessRecord record = new BusinessRecord();
        record.setStaffId(staffId);
        record.setStaffName(getNameById(staffId));
        if (member != null) {
            record.setMemberId(member.getId());
            record.setMemberName(member.getName());
        }
        record.setRecordType(recordType);
        record.setAmount(BigDecimal.ZERO);
        record.setPrincipalAmount(BigDecimal.ZERO);
        record.setBonusAmount(BigDecimal.ZERO);
        record.setPointsAmount(0);
        record.setWineQuantity(0);
        record.setChipAmount(0);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    private void notifyMemberChanged(Long memberId) {
        userDataUpdateNotifier.notifyAfterCommit(memberId);
        staffDataUpdateNotifier.notifyAfterCommit(memberId);
    }

    private record PackagePlan(int amount, int wine, int chips) {}

    private record GroupBuyPlan(int revenue, int chips) {}

    private record DeductResult(BigDecimal principal, BigDecimal bonus, BigDecimal shortfall) {}
}
