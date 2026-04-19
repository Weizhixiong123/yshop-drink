package com.clubhub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.entity.Staff;
import com.clubhub.enums.OperationType;
import com.clubhub.mapper.OperationLogMapper;
import com.clubhub.service.MemberService;
import com.clubhub.service.StaffService;
import com.clubhub.service.StatService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final StaffService staffService;
    private final MemberService memberService;
    private final StatService statService;
    private final OperationLogMapper operationLogMapper;

    // ========== 员工管理 ==========

    @PostMapping("/staff/add")
    public Result<?> addStaff(@RequestBody @Valid Staff staff) {
        staff.setRole("staff");
        return staffService.addStaff(staff);
    }

    @GetMapping("/staff/list")
    public Result<?> listStaff() {
        return staffService.listStaff();
    }

    @PutMapping("/staff/status")
    public Result<?> updateStaffStatus(@RequestParam Long id, @RequestParam Integer status) {
        return staffService.updateStatus(id, status);
    }

    @DeleteMapping("/staff/{id}")
    public Result<?> deleteStaff(@PathVariable Long id) {
        return staffService.deleteStaff(id);
    }

    @PutMapping("/staff/password/reset")
    public Result<?> resetStaffPassword(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String newPassword = params.get("newPassword") == null ? null : params.get("newPassword").toString();
        return staffService.resetPassword(id, newPassword);
    }

    // ========== 操作日志 ==========

    @GetMapping("/log/list")
    public Result<?> logList(@RequestParam(required = false) Long memberId,
                             @RequestParam(required = false) Long staffId,
                             @RequestParam(required = false) String operationType,
                             @RequestParam(required = false) String startTime,
                             @RequestParam(required = false) String endTime,
                             @RequestParam(defaultValue = "1") long pageNum,
                             @RequestParam(defaultValue = "20") long pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = buildLogQuery(memberId, staffId, operationType, startTime, endTime);
        Page<OperationLog> page = operationLogMapper.selectPage(Page.of(pageNum, pageSize), wrapper);
        return Result.ok(Map.of(
                "total", page.getTotal(),
                "pageNum", page.getCurrent(),
                "pageSize", page.getSize(),
                "list", page.getRecords()
        ));
    }

    private LambdaQueryWrapper<OperationLog> buildLogQuery(Long memberId, Long staffId,
                                                            String operationType,
                                                            String startTime, String endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) wrapper.eq(OperationLog::getMemberId, memberId);
        if (staffId != null) wrapper.eq(OperationLog::getStaffId, staffId);
        if (operationType != null && !operationType.isBlank()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isBlank()) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(startTime, fmt));
        }
        if (endTime != null && !endTime.isBlank()) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(endTime, fmt));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return wrapper;
    }

    // ========== 统计 ==========

    @GetMapping("/stat/today")
    public Result<?> todayStat() {
        return statService.todayStat();
    }

    // ========== 客户资料下载 ==========

    @GetMapping("/member/export")
    public void exportMembers(HttpServletResponse response) throws IOException {
        List<Member> members = memberService.listAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("客户资料");

        // 表头
        String[] headers = {"姓名", "性别", "手机号", "存酒", "积分", "储值余额", "备注"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // 数据
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(m.getName());
            row.createCell(1).setCellValue("M".equals(m.getGender()) ? "男" : "女");
            row.createCell(2).setCellValue(m.getPhone());
            row.createCell(3).setCellValue(m.getWine());
            row.createCell(4).setCellValue(m.getPoints());
            row.createCell(5).setCellValue(m.getBalance().doubleValue());
            row.createCell(6).setCellValue(m.getRemark() != null ? m.getRemark() : "");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("客户资料.xlsx", StandardCharsets.UTF_8));
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ========== 操作日志导出 ==========

    @GetMapping("/log/export")
    public void exportLogs(@RequestParam(required = false) Long memberId,
                           @RequestParam(required = false) Long staffId,
                           @RequestParam(required = false) String operationType,
                           @RequestParam(required = false) String startTime,
                           @RequestParam(required = false) String endTime,
                           HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<OperationLog> wrapper = buildLogQuery(memberId, staffId, operationType, startTime, endTime);
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("操作日志");

        String[] headers = {"时间", "操作员工", "客户", "操作类型", "操作值", "变更前", "变更后"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (int i = 0; i < logs.size(); i++) {
            OperationLog log = logs.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(log.getCreateTime() == null ? "" : log.getCreateTime().format(fmt));
            row.createCell(1).setCellValue(log.getStaffName());
            row.createCell(2).setCellValue(log.getMemberName());
            row.createCell(3).setCellValue(OperationType.nameOf(log.getOperationType()));
            row.createCell(4).setCellValue(log.getOperationValue().doubleValue());
            row.createCell(5).setCellValue(log.getBeforeValue().doubleValue());
            row.createCell(6).setCellValue(log.getAfterValue().doubleValue());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("操作日志.xlsx", StandardCharsets.UTF_8));
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
