package com.clubhub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.enums.OperationType;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final OperationLogMapper operationLogMapper;

    /**
     * 新客开卡
     */
    public Result<?> register(Member member) {
        // 手机号查重
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, member.getPhone()));
        if (count > 0) {
            return Result.fail("该手机号已开卡，请勿重复开卡");
        }
        if (member.getWine() == null) member.setWine(0);
        if (member.getPoints() == null) member.setPoints(0);
        if (member.getBalance() == null) member.setBalance(BigDecimal.ZERO);
        member.setDeleted(0);
        memberMapper.insert(member);
        return Result.ok();
    }

    /**
     * 按手机号后四位搜索
     */
    public Result<?> searchByPhoneTail(String tail, String role) {
        List<Member> list = memberMapper.selectList(
                new LambdaQueryWrapper<Member>().likeLeft(Member::getPhone, tail));

        if (list.isEmpty()) {
            return Result.ok(List.of());
        }

        // 检查尾号是否有重复
        boolean hasDuplicate = list.size() > 1;

        List<Map<String, Object>> result = list.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("wine", m.getWine());
            map.put("points", m.getPoints());
            map.put("balance", m.getBalance());
            map.put("remark", m.getRemark());

            // 店东始终显示完整手机号；店员仅在尾号重复时显示完整手机号
            if ("owner".equals(role) || hasDuplicate) {
                map.put("phone", m.getPhone());
            } else {
                map.put("phone", "****" + m.getPhone().substring(m.getPhone().length() - 4));
            }
            return map;
        }).toList();

        return Result.ok(result);
    }

    /**
     * 获取会员详情
     */
    public Result<?> detail(Long id, String role) {
        Member m = memberMapper.selectById(id);
        if (m == null) {
            return Result.fail("客户不存在");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("gender", m.getGender());
        map.put("wine", m.getWine());
        map.put("points", m.getPoints());
        map.put("balance", m.getBalance());
        map.put("remark", m.getRemark());

        if ("owner".equals(role)) {
            map.put("phone", m.getPhone());
        } else {
            // 店员查看详情时，检查是否有尾号重复
            String tail = m.getPhone().substring(m.getPhone().length() - 4);
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>().likeLeft(Member::getPhone, tail));
            if (count > 1) {
                map.put("phone", m.getPhone());
            } else {
                map.put("phone", "****" + tail);
            }
        }

        return Result.ok(map);
    }

    /**
     * 加减操作（积分/酒/储值）
     */
    @Transactional
    public Result<?> operate(Long memberId, OperationType type, BigDecimal value,
                             Long staffId, String staffName) {
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

        // 记录操作日志
        OperationLog log = new OperationLog();
        log.setStaffId(staffId);
        log.setStaffName(staffName);
        log.setMemberId(memberId);
        log.setMemberName(member.getName());
        log.setOperationType(type.getCode());
        log.setOperationValue(value);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("before", beforeValue);
        result.put("after", afterValue);
        return Result.ok(result);
    }

    /**
     * 修改备注
     */
    public Result<?> updateRemark(Long id, String remark) {
        if (memberMapper.selectById(id) == null) {
            return Result.fail("客户不存在");
        }
        Member member = new Member();
        member.setId(id);
        member.setRemark(remark);
        memberMapper.updateById(member);
        return Result.ok();
    }

    /**
     * 修改基本信息（姓名/性别/手机号）
     * 仅传了的字段会被更新；手机号变更需查重
     */
    public Result<?> updateInfo(Long id, String name, String gender, String phone) {
        Member exist = memberMapper.selectById(id);
        if (exist == null) {
            return Result.fail("客户不存在");
        }
        Member update = new Member();
        update.setId(id);

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
                            .ne(Member::getId, id));
            if (count > 0) {
                return Result.fail("该手机号已被其他会员使用");
            }
            update.setPhone(phone);
        }
        memberMapper.updateById(update);
        return Result.ok();
    }

    /**
     * 获取所有会员（店东下载用）
     */
    public List<Member> listAll() {
        return memberMapper.selectList(new LambdaQueryWrapper<Member>().orderByDesc(Member::getCreateTime));
    }

    /**
     * 客户自助查看
     */
    public Result<?> customerView(String phone) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (member == null) {
            return Result.fail("未找到会员信息");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", member.getName());
        map.put("wine", member.getWine());
        map.put("points", member.getPoints());
        map.put("balance", member.getBalance());
        map.put("remark", member.getRemark());
        return Result.ok(map);
    }
}
