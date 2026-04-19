package com.clubhub.owner.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StaffResponse {
    private Long id;
    private String phone;
    private String name;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}
