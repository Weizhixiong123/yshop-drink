package com.clubhub.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member")
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过 50 字")
    private String name;

    /** M=男, F=女 */
    @Pattern(regexp = "^[MF]$", message = "性别只能是 M 或 F")
    private String gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 当前存酒数量 */
    private Integer wine;

    /** 当前积分 */
    private Integer points;

    /** 当前储值余额 */
    private BigDecimal balance;

    /** 备注（SA证、私酒、特殊说明等） */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
