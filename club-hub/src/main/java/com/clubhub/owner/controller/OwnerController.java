package com.clubhub.owner.controller;

import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.enums.OperationType;
import com.clubhub.owner.request.OperationLogQueryRequest;
import com.clubhub.owner.request.StaffAddRequest;
import com.clubhub.owner.request.StaffPasswordResetRequest;
import com.clubhub.owner.service.OwnerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    // ========== 员工管理 ==========

    @PostMapping("/staff/add")
    public Result<?> addStaff(@RequestBody @Valid StaffAddRequest request) {
        return ownerService.addStaff(request);
    }

    @GetMapping("/staff/list")
    public Result<?> listStaff() {
        return ownerService.listStaff();
    }

    @PutMapping("/staff/status")
    public Result<?> updateStaffStatus(@RequestParam Long id, @RequestParam Integer status) {
        return ownerService.updateStaffStatus(id, status);
    }

    @DeleteMapping("/staff/{id}")
    public Result<?> deleteStaff(@PathVariable Long id) {
        return ownerService.deleteStaff(id);
    }

    @PutMapping("/staff/password/reset")
    public Result<?> resetStaffPassword(@RequestBody @Valid StaffPasswordResetRequest request) {
        return ownerService.resetStaffPassword(request);
    }

    // ========== 操作日志 ==========

    @GetMapping("/log/list")
    public Result<?> logList(OperationLogQueryRequest query) {
        if (query.getPageNum() == null) query.setPageNum(1L);
        if (query.getPageSize() == null) query.setPageSize(20L);
        return ownerService.listLogs(query);
    }

    // ========== 统计 ==========

    @GetMapping("/stat/today")
    public Result<?> todayStat() {
        return ownerService.todayStat();
    }

    // ========== 客户资料下载 ==========

    @GetMapping("/member/export")
    public void exportMembers(HttpServletResponse response) throws IOException {
        List<Member> members = ownerService.listAllMembers();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("客户资料");

        String[] headers = {"姓名", "性别", "手机号", "存酒", "积分", "储值余额", "备注"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

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
    public void exportLogs(OperationLogQueryRequest query, HttpServletResponse response) throws IOException {
        List<OperationLog> logs = ownerService.listLogsForExport(query);
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
