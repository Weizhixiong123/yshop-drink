package com.clubhub.owner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.entity.BusinessRecord;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.OperationLog;
import com.clubhub.enums.OperationType;
import com.clubhub.mapper.BusinessRecordMapper;
import com.clubhub.owner.request.MemberLevelUpdateRequest;
import com.clubhub.owner.request.MemberQueryRequest;
import com.clubhub.owner.request.OperationLogQueryRequest;
import com.clubhub.owner.request.StaffAddRequest;
import com.clubhub.owner.request.StaffUpdateRequest;
import com.clubhub.owner.service.OwnerService;
import com.clubhub.staff.request.MemberInfoUpdateRequest;
import com.clubhub.staff.request.MemberOperateRequest;
import com.clubhub.staff.request.MemberRemarkUpdateRequest;
import com.clubhub.staff.service.StaffService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final StaffService staffService;
    private final BusinessRecordMapper businessRecordMapper;

    // ========== 员工管理 ==========

    @PostMapping("/staff/add")
    public Result<?> addStaff(@RequestBody @Valid StaffAddRequest request) {
        return ownerService.addStaff(request);
    }

    @GetMapping("/staff/list")
    public Result<?> listStaff() {
        return ownerService.listStaff();
    }

    @PutMapping("/staff/update")
    public Result<?> updateStaff(@RequestBody @Valid StaffUpdateRequest request) {
        return ownerService.updateStaff(request);
    }

    @DeleteMapping("/staff/{id}")
    public Result<?> deleteStaff(@PathVariable String id) {
        return ownerService.deleteStaff(id);
    }

    // ========== 操作日志 ==========

    @GetMapping("/log/list")
    public Result<?> logList(OperationLogQueryRequest query) {
        if (query.getPageNum() == null) query.setPageNum(1L);
        if (query.getPageSize() == null) query.setPageSize(10L);
        return ownerService.listLogs(query);
    }

    // ========== 统计 ==========

    @GetMapping("/stat/today")
    public Result<?> todayStat() {
        return ownerService.todayStat();
    }

    @GetMapping("/account/stat")
    public Result<?> accountStat(@RequestParam(required = false) String date) {
        return ownerService.accountStat(date);
    }

    @PutMapping("/member/level")
    public Result<?> updateMemberLevel(@RequestBody @Valid MemberLevelUpdateRequest request) {
        return ownerService.updateMemberLevel(request);
    }

    // ========== 客户资料下载 ==========

    @GetMapping("/member/list")
    public Result<?> memberList(MemberQueryRequest query) {
        if (query.getPageNum() == null) query.setPageNum(1L);
        return ownerService.listMembers(query);
    }

    @PutMapping("/member/info")
    public Result<?> updateMemberInfo(@RequestBody @Valid MemberInfoUpdateRequest request) {
        return staffService.updateMemberInfo(request);
    }

    @PutMapping("/member/remark")
    public Result<?> updateMemberRemark(@RequestBody @Valid MemberRemarkUpdateRequest request) {
        return staffService.updateMemberRemark(request);
    }

    @PostMapping("/member/operate")
    public Result<?> operateMember(@RequestBody @Valid MemberOperateRequest request,
                                   HttpServletRequest httpRequest) {
        String staffId = (String) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        return staffService.operate(request, staffId, role);
    }

    @GetMapping("/member/export")
    public void exportMembers(HttpServletResponse response) throws IOException {
        List<Member> members = ownerService.listAllMembers();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("客户资料");

        String[] headers = {"姓名", "性别", "手机号", "存酒", "积分", "储值余额", "本金余额", "赠送余额", "会员等级", "备注"};
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
            row.createCell(5).setCellValue(m.getBalance() == null ? 0 : m.getBalance().doubleValue());
            row.createCell(6).setCellValue(m.getPrincipalBalance() == null ? 0 : m.getPrincipalBalance().doubleValue());
            row.createCell(7).setCellValue(m.getBonusBalance() == null ? 0 : m.getBonusBalance().doubleValue());
            row.createCell(8).setCellValue(m.getLevel() == null ? "normal" : m.getLevel());
            row.createCell(9).setCellValue(m.getRemark() != null ? m.getRemark() : "");
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

    @GetMapping("/account/export")
    public void exportAccount(@RequestParam(required = false) String date,
                              HttpServletResponse response) throws IOException {
        LocalDate statDate = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();
        List<BusinessRecord> records = businessRecordMapper.selectList(
                new LambdaQueryWrapper<BusinessRecord>()
                        .ge(BusinessRecord::getCreateTime, start)
                        .lt(BusinessRecord::getCreateTime, end)
                        .orderByDesc(BusinessRecord::getCreateTime));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("门店对账");
        String[] headers = {"时间", "类型", "操作人", "会员", "营收/扣款", "本金", "赠送", "积分", "酒水", "码量(千)", "套餐/档位", "备注"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        for (int i = 0; i < records.size(); i++) {
            BusinessRecord r = records.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(r.getCreateTime() == null ? "" : r.getCreateTime().format(fmt));
            row.createCell(1).setCellValue(r.getRecordType());
            row.createCell(2).setCellValue(r.getStaffName());
            row.createCell(3).setCellValue(r.getMemberName() == null ? "" : r.getMemberName());
            row.createCell(4).setCellValue(r.getAmount() == null ? 0 : r.getAmount().doubleValue());
            row.createCell(5).setCellValue(r.getPrincipalAmount() == null ? 0 : r.getPrincipalAmount().doubleValue());
            row.createCell(6).setCellValue(r.getBonusAmount() == null ? 0 : r.getBonusAmount().doubleValue());
            row.createCell(7).setCellValue(r.getPointsAmount() == null ? 0 : r.getPointsAmount());
            row.createCell(8).setCellValue(r.getWineQuantity() == null ? 0 : r.getWineQuantity());
            row.createCell(9).setCellValue(r.getChipAmount() == null ? 0 : r.getChipAmount());
            row.createCell(10).setCellValue(r.getPackageCode() == null ? "" : r.getPackageCode());
            row.createCell(11).setCellValue(r.getRemark() == null ? "" : r.getRemark());
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("门店对账-" + statDate + ".xlsx", StandardCharsets.UTF_8));
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
