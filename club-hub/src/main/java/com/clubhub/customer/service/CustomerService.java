package com.clubhub.customer.service;

import com.clubhub.dto.Result;

public interface CustomerService {

    /**
     * 客户自助查看自己的信息
     */
    Result<?> viewMyInfo(String phone);
}
