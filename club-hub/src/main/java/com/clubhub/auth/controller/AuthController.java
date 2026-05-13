package com.clubhub.auth.controller;

import com.clubhub.auth.request.CustomerLoginRequest;
import com.clubhub.auth.request.CustomerWxLoginRequest;
import com.clubhub.auth.request.StaffLoginRequest;
import com.clubhub.auth.request.StaffWxLoginRequest;
import com.clubhub.auth.service.AuthService;
import com.clubhub.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 店东账号密码登录
     */
    @PostMapping("/staff/login")
    public Result<?> staffLogin(@RequestBody @Valid StaffLoginRequest request) {
        return authService.staffLogin(request);
    }

    /**
     * 店员微信手机号授权登录
     */
    @PostMapping("/staff/wx-login")
    public Result<?> staffWxLogin(@RequestBody @Valid StaffWxLoginRequest request) {
        return authService.staffWxLogin(request);
    }

    /**
     * 客户手机号登录
     */
    @PostMapping("/customer/login")
    public Result<?> customerLogin(@RequestBody @Valid CustomerLoginRequest request) {
        return authService.customerLogin(request);
    }

    /**
     * 客户微信手机号授权登录
     */
    @PostMapping("/customer/wx-login")
    public Result<?> customerWxLogin(@RequestBody @Valid CustomerWxLoginRequest request) {
        return authService.customerWxLogin(request);
    }
}
