package com.clubhub.owner.request;

import lombok.Data;

@Data
public class MemberQueryRequest {
    private Long id;
    private String name;
    private String gender;
    private String phone;
    private Long pageNum;
    private Long pageSize;
}
