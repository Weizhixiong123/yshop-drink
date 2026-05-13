package com.clubhub.auth.service;

import com.clubhub.auth.request.CustomerLoginRequest;
import com.clubhub.auth.request.CustomerWxLoginRequest;
import com.clubhub.auth.request.StaffLoginRequest;
import com.clubhub.auth.request.StaffWxLoginRequest;
import com.clubhub.dto.Result;

public interface AuthService {

    /**
     * 店东账号密码登录
     */
    Result<?> staffLogin(StaffLoginRequest request);

    /**
     * 店员微信手机号授权登录
     */
    Result<?> staffWxLogin(StaffWxLoginRequest request);

    /**
     * 客户手机号登录
     */
    Result<?> customerLogin(CustomerLoginRequest request);

    /**
     * 客户微信手机号授权登录
     */
    Result<?> customerWxLogin(CustomerWxLoginRequest request);
}
