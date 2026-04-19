package com.clubhub.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 50, message = "账号长度需在 3~50 之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在 6~100 之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过 50 字")
    private String name;

    /** owner=店东, staff=店员 */
    private String role;

    /** 0=禁用, 1=启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
