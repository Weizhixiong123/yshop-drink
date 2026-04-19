package com.clubhub.controller;

import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.enums.OperationType;
import com.clubhub.service.MemberService;
import com.clubhub.service.StaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final MemberService memberService;
    private final StaffService staffService;

    /**
     * 新客开卡
     */
    @PostMapping("/member/register")
    public Result<?> register(@RequestBody @Valid Member member) {
        return memberService.register(member);
    }

    /**
     * 按手机号后四位搜索
     */
    @GetMapping("/member/search")
    public Result<?> search(@RequestParam String tail, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return memberService.searchByPhoneTail(tail, role);
    }

    /**
     * 会员详情
     */
    @GetMapping("/member/detail/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return memberService.detail(id, role);
    }

    /**
     * 加减操作
     */
    @PostMapping("/member/operate")
    public Result<?> operate(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long memberId = Long.valueOf(params.get("memberId").toString());
        OperationType type = OperationType.valueOf(params.get("type").toString());
        BigDecimal value = new BigDecimal(params.get("value").toString());
        Long staffId = (Long) request.getAttribute("userId");
        String staffName = staffService.getName(staffId);

        return memberService.operate(memberId, type, value, staffId, staffName);
    }

    /**
     * 修改备注
     */
    @PutMapping("/member/remark")
    public Result<?> updateRemark(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String remark = params.get("remark") == null ? null : params.get("remark").toString();
        return memberService.updateRemark(id, remark);
    }

    /**
     * 修改会员基本信息（姓名/性别/手机号）
     */
    @PutMapping("/member/info")
    public Result<?> updateInfo(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String name = params.get("name") == null ? null : params.get("name").toString();
        String gender = params.get("gender") == null ? null : params.get("gender").toString();
        String phone = params.get("phone") == null ? null : params.get("phone").toString();
        return memberService.updateInfo(id, name, gender, phone);
    }

    /**
     * 员工修改自己密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params,
                                    HttpServletRequest request) {
        Long staffId = (Long) request.getAttribute("userId");
        return staffService.changePassword(staffId, params.get("oldPassword"), params.get("newPassword"));
    }
}
