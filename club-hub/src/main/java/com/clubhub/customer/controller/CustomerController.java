package com.clubhub.customer.controller;

import com.clubhub.customer.service.CustomerService;
import com.clubhub.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 客户自助查看自己的信息
     */
    @GetMapping("/info")
    public Result<?> myInfo(@RequestParam String phone) {
        return customerService.viewMyInfo(phone);
    }
}
