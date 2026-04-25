package com.clubhub.owner.response;

import com.clubhub.entity.Member;
import lombok.Data;

import java.util.List;

@Data
public class MemberPageResponse {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<Member> list;
}
