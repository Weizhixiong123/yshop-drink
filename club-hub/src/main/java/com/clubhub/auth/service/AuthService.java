package com.clubhub.auth.service;

import com.clubhub.auth.request.CustomerLoginRequest;
import com.clubhub.auth.request.StaffLoginRequest;
import com.clubhub.dto.Result;

public interface AuthService {

    /**
     * 店东/店员登录
     */
    Result<?> staffLogin(StaffLoginRequest request);

    /**
     * 客户手机号登录
     */
    Result<?> customerLogin(CustomerLoginRequest request);
}
