package com.clubhub.service;

import com.clubhub.dto.Result;
import com.clubhub.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 今日统计（12:00~次日12:00）
     */
    public Result<?> todayStat() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;

        // 如果当前时间在12:00之前，统计日为昨天12:00~今天12:00
        if (now.toLocalTime().isBefore(LocalTime.NOON)) {
            start = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.NOON);
            end = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
        } else {
            // 当前时间在12:00之后，统计日为今天12:00~明天12:00
            start = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
            end = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON);
        }

        BigDecimal totalAddPoints = operationLogMapper.sumAddPoints(start, end);
        BigDecimal totalSpecifiedSubPoints = operationLogMapper.sumSpecifiedSubPoints(start, end);
        BigDecimal chipValue = totalSpecifiedSubPoints.divide(BigDecimal.valueOf(2));

        return Result.ok(Map.of(
                "statDate", start.toLocalDate().toString() + " 12:00 ~ " + end.toLocalDate().toString() + " 12:00",
                "totalAddPoints", totalAddPoints,
                "totalSpecifiedSubPoints", totalSpecifiedSubPoints,
                "chipValue", chipValue
        ));
    }
}
