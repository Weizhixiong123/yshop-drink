package com.clubhub.staff.controller;

import com.clubhub.dto.Result;
import com.clubhub.staff.request.MemberInfoUpdateRequest;
import com.clubhub.staff.request.MemberOperateRequest;
import com.clubhub.staff.request.MemberRegisterRequest;
import com.clubhub.staff.request.MemberRemarkUpdateRequest;
import com.clubhub.staff.service.StaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    /**
     * 新客开卡
     */
    @PostMapping("/member/register")
    public Result<?> register(@RequestBody @Valid MemberRegisterRequest request) {
        return staffService.registerMember(request);
    }

    /**
     * 按手机号后四位搜索
     */
    @GetMapping("/member/search")
    public Result<?> search(@RequestParam String tail, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return staffService.searchByPhoneTail(tail, role);
    }

    /**
     * 会员详情
     */
    @GetMapping("/member/detail/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return staffService.memberDetail(id, role);
    }

    /**
     * 加减操作
     */
    @PostMapping("/member/operate")
    public Result<?> operate(@RequestBody @Valid MemberOperateRequest request,
                             HttpServletRequest httpRequest) {
        Long staffId = (Long) httpRequest.getAttribute("userId");
        return staffService.operate(request, staffId);
    }

    /**
     * 修改备注
     */
    @PutMapping("/member/remark")
    public Result<?> updateRemark(@RequestBody @Valid MemberRemarkUpdateRequest request) {
        return staffService.updateMemberRemark(request);
    }

    /**
     * 修改会员基本信息（姓名/性别/手机号）
     */
    @PutMapping("/member/info")
    public Result<?> updateInfo(@RequestBody @Valid MemberInfoUpdateRequest request) {
        return staffService.updateMemberInfo(request);
    }

}
