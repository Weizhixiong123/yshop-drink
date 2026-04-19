package com.clubhub.staff.service;

import com.clubhub.dto.Result;
import com.clubhub.entity.Staff;
import com.clubhub.staff.request.MemberInfoUpdateRequest;
import com.clubhub.staff.request.MemberOperateRequest;
import com.clubhub.staff.request.MemberRegisterRequest;
import com.clubhub.staff.request.MemberRemarkUpdateRequest;

public interface StaffService {

    /**
     * 新客开卡
     */
    Result<?> registerMember(MemberRegisterRequest request);

    /**
     * 按手机号后四位搜索
     */
    Result<?> searchByPhoneTail(String tail, String role);

    /**
     * 会员详情
     */
    Result<?> memberDetail(Long id, String role);

    /**
     * 积分/存酒/储值加减操作
     */
    Result<?> operate(MemberOperateRequest request, Long staffId);

    /**
     * 修改备注
     */
    Result<?> updateMemberRemark(MemberRemarkUpdateRequest request);

    /**
     * 修改会员基本信息（姓名/性别/手机号）
     */
    Result<?> updateMemberInfo(MemberInfoUpdateRequest request);

    /**
     * 根据ID获取员工姓名
     */
    String getNameById(Long staffId);

    /**
     * 根据ID获取员工
     */
    Staff getById(Long staffId);
}
