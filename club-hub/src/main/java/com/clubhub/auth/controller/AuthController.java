package com.clubhub.auth.controller;

import com.clubhub.auth.request.CustomerLoginRequest;
import com.clubhub.auth.request.StaffLoginRequest;
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
     * 店东/店员登录
     */
    @PostMapping("/staff/login")
    public Result<?> staffLogin(@RequestBody @Valid StaffLoginRequest request) {
        return authService.staffLogin(request);
    }

    /**
     * 客户手机号登录
     */
    @PostMapping("/customer/login")
    public Result<?> customerLogin(@RequestBody @Valid CustomerLoginRequest request) {
        return authService.customerLogin(request);
    }
}
