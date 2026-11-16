package com.huimai.module.report.controller;

import com.huimai.common.Result;
import com.huimai.module.report.dto.ReportCreateDTO;
import com.huimai.module.report.service.ReportService;
import com.huimai.module.report.vo.ReportVO;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 举报接口（需登录）。
 */
@Tag(name = "举报接口")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "提交举报")
    @PostMapping
    public Result<Void> submit(@Valid @RequestBody ReportCreateDTO dto) {
        reportService.submit(dto, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @Operation(summary = "举报列表（后台）")
    @GetMapping("/list")
    public Result<List<ReportVO>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(reportService.list(status));
    }

    @Operation(summary = "处置举报")
    @PutMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable Long id, @RequestParam Integer status) {
        reportService.handle(id, status);
        return Result.ok();
    }
}
