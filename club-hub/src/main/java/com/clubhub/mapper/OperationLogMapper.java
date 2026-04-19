package com.clubhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clubhub.entity.OperationLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Select("SELECT COALESCE(SUM(operation_value), 0) FROM operation_log " +
            "WHERE operation_type = 'add_points' AND create_time BETWEEN #{start} AND #{end}")
    BigDecimal sumAddPoints(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT COALESCE(SUM(operation_value), 0) FROM operation_log " +
            "WHERE operation_type = 'sub_points' AND operation_value IN (100, 200, 400) " +
            "AND create_time BETWEEN #{start} AND #{end}")
    BigDecimal sumSpecifiedSubPoints(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
