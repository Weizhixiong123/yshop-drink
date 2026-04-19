package com.clubhub.controller;

import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.service.MemberService;
import com.clubhub.service.StaffService;
import com.clubhub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StaffService staffService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    /**
     * 店东/店员登录
     */
    @PostMapping("/staff/login")
    public Result<?> staffLogin(@RequestBody Map<String, String> params) {
        return staffService.login(params.get("username"), params.get("password"));
    }

    /**
     * 客户手机号登录
     */
    @PostMapping("/customer/login")
    public Result<?> customerLogin(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        Result<?> result = memberService.customerView(phone);
        if (result.getCode() != 200) {
            return result;
        }
        // 查找会员ID用于生成token
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Member> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, phone);
        // 生成token时使用手机号的hash作为临时ID
        String token = jwtUtil.generateToken(Long.valueOf(phone.hashCode() & 0x7fffffffL), "customer");
        return Result.ok(Map.of("token", token, "phone", phone));
    }
}
