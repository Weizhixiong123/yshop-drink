package com.clubhub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.auth.request.CustomerLoginRequest;
import com.clubhub.auth.request.StaffLoginRequest;
import com.clubhub.auth.response.CustomerLoginResponse;
import com.clubhub.auth.response.StaffLoginResponse;
import com.clubhub.auth.service.AuthService;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.Staff;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StaffMapper staffMapper;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;

    @Override
    public Result<?> staffLogin(StaffLoginRequest request) {
        Staff staff = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>()
                        .eq(Staff::getUsername, request.getUsername())
                        .eq(Staff::getStatus, 1));
        if (staff == null || !staff.getPassword().equals(request.getPassword())) {
            return Result.fail("账号或密码错误");
        }
        String token = jwtUtil.generateToken(staff.getId(), staff.getRole());
        return Result.ok(new StaffLoginResponse(token, staff.getName(), staff.getRole()));
    }

    @Override
    public Result<?> customerLogin(CustomerLoginRequest request) {
        String phone = request.getPhone();
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (member == null) {
            return Result.fail("未找到会员信息");
        }
        String token = jwtUtil.generateToken(Long.valueOf(phone.hashCode() & 0x7fffffffL), "customer");
        return Result.ok(new CustomerLoginResponse(token, phone));
    }
}
