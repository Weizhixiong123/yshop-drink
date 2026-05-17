package com.clubhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("business_record")
public class BusinessRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String staffId;
    private String staffName;
    private Long memberId;
    private String memberName;
    private String recordType;
    private BigDecimal amount;
    private BigDecimal principalAmount;
    private BigDecimal bonusAmount;
    private Integer pointsAmount;
    private Integer wineQuantity;
    private Integer chipAmount;
    private String packageCode;
    private String remark;
    private LocalDateTime createTime;
}
