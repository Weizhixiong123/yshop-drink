package com.clubhub.staff.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.entity.Staff;
import com.clubhub.enums.OperationType;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.OperationLogMapper;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.staff.request.MemberInfoUpdateRequest;
import com.clubhub.staff.request.MemberOperateRequest;
import com.clubhub.staff.request.MemberRegisterRequest;
import com.clubhub.staff.request.MemberRemarkUpdateRequest;
import com.clubhub.staff.response.MemberDetailResponse;
import com.clubhub.staff.response.MemberOperateResponse;
import com.clubhub.staff.response.MemberSearchItemResponse;
import com.clubhub.staff.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffMapper staffMapper;
    private final MemberMapper memberMapper;
    private final OperationLogMapper operationLogMapper;

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
        member.setDeleted(0);
        memberMapper.insert(member);
        return Result.ok();
    }

    @Override
    public Result<?> searchByPhoneTail(String tail, String role) {
        List<Member> list = memberMapper.selectList(
                new LambdaQueryWrapper<Member>().likeLeft(Member::getPhone, tail));

        if (list.isEmpty()) {
            return Result.ok(List.of());
        }

        boolean hasDuplicate = list.size() > 1;

        List<MemberSearchItemResponse> result = list.stream().map(m -> {
            MemberSearchItemResponse item = new MemberSearchItemResponse();
            item.setId(m.getId());
            item.setName(m.getName());
            item.setWine(m.getWine());
            item.setPoints(m.getPoints());
            item.setBalance(m.getBalance());
            item.setRemark(m.getRemark());

            if ("owner".equals(role) || hasDuplicate) {
                item.setPhone(m.getPhone());
            } else {
                item.setPhone("****" + m.getPhone().substring(m.getPhone().length() - 4));
            }
            return item;
        }).toList();

        return Result.ok(result);
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
        detail.setRemark(m.getRemark());

        if ("owner".equals(role)) {
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
    public Result<?> operate(MemberOperateRequest request, String staffId) {
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
                member.setBalance(member.getBalance().add(value));
                afterValue = member.getBalance();
            }
            case SUB_BALANCE -> {
                beforeValue = member.getBalance();
                if (member.getBalance().compareTo(value) < 0) {
                    return Result.fail("储值余额不足");
                }
                member.setBalance(member.getBalance().subtract(value));
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

        return Result.ok(new MemberOperateResponse(beforeValue, afterValue));
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
}
