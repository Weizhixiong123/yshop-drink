package com.clubhub.controller;

import com.clubhub.dto.Result;
import com.clubhub.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final MemberService memberService;

    /**
     * 客户自助查看自己的信息
     */
    @GetMapping("/info")
    public Result<?> myInfo(@RequestParam String phone) {
        return memberService.customerView(phone);
    }
}
