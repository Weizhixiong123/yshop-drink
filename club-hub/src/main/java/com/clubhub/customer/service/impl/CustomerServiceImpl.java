package com.clubhub.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.customer.response.CustomerInfoResponse;
import com.clubhub.customer.service.CustomerService;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final MemberMapper memberMapper;

    @Override
    public Result<?> viewMyInfo(String phone) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (member == null) {
            return Result.fail("未找到会员信息");
        }
        CustomerInfoResponse resp = new CustomerInfoResponse();
        resp.setName(member.getName());
        resp.setWine(member.getWine());
        resp.setPoints(member.getPoints());
        resp.setBalance(member.getBalance());
        resp.setRemark(member.getRemark());
        return Result.ok(resp);
    }
}
