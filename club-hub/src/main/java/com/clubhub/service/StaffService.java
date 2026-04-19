package com.clubhub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.dto.Result;
import com.clubhub.entity.Staff;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffMapper staffMapper;
    private final JwtUtil jwtUtil;

    public Result<?> login(String username, String password) {
        Staff staff = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>()
                        .eq(Staff::getUsername, username)
                        .eq(Staff::getStatus, 1));
        if (staff == null || !staff.getPassword().equals(password)) {
            return Result.fail("账号或密码错误");
        }
        String token = jwtUtil.generateToken(staff.getId(), staff.getRole());
        return Result.ok(java.util.Map.of(
                "token", token,
                "name", staff.getName(),
                "role", staff.getRole()));
    }

    public Result<?> addStaff(Staff staff) {
        Staff exist = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, staff.getUsername()));
        if (exist != null) {
            return Result.fail("账号已存在");
        }
        staff.setStatus(1);
        staff.setDeleted(0);
        staffMapper.insert(staff);
        return Result.ok();
    }

    public Result<?> listStaff() {
        List<Staff> list = staffMapper.selectList(
                new LambdaQueryWrapper<Staff>().eq(Staff::getRole, "staff").orderByDesc(Staff::getCreateTime));
        list.forEach(s -> s.setPassword(null));
        return Result.ok(list);
    }

    public Result<?> updateStatus(Long id, Integer status) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setStatus(status);
        staffMapper.updateById(staff);
        return Result.ok();
    }

    public Result<?> deleteStaff(Long id) {
        staffMapper.deleteById(id);
        return Result.ok();
    }

    public String getName(Long id) {
        Staff staff = staffMapper.selectById(id);
        return staff != null ? staff.getName() : "未知员工";
    }

    public Staff getById(Long id) {
        return staffMapper.selectById(id);
    }

    /**
     * 员工改自己密码
     */
    public Result<?> changePassword(Long staffId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码至少 6 位");
        }
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            return Result.fail("用户不存在");
        }
        if (!staff.getPassword().equals(oldPassword)) {
            return Result.fail("原密码错误");
        }
        Staff update = new Staff();
        update.setId(staffId);
        update.setPassword(newPassword);
        staffMapper.updateById(update);
        return Result.ok();
    }

    /**
     * 店东重置店员密码
     */
    public Result<?> resetPassword(Long staffId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码至少 6 位");
        }
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            return Result.fail("员工不存在");
        }
        if ("owner".equals(staff.getRole())) {
            return Result.fail("不能通过此接口重置店东密码");
        }
        Staff update = new Staff();
        update.setId(staffId);
        update.setPassword(newPassword);
        staffMapper.updateById(update);
        return Result.ok();
    }
}
