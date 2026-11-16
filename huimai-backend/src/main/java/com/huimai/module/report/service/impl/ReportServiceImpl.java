package com.huimai.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huimai.common.BusinessException;
import com.huimai.module.report.dto.ReportCreateDTO;
import com.huimai.module.report.entity.Report;
import com.huimai.module.report.mapper.ReportMapper;
import com.huimai.module.report.service.ReportService;
import com.huimai.module.report.vo.ReportVO;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 举报服务实现。
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(ReportCreateDTO dto, Long userId) {
        if (dto.getVideoId() == null && dto.getCommentId() == null) {
            throw new BusinessException("请选择要举报的内容");
        }
        Report report = new Report();
        report.setVideoId(dto.getVideoId());
        report.setCommentId(dto.getCommentId());
        report.setUserId(userId);
        report.setReason(dto.getReason().trim());
        report.setDescription(dto.getDescription());
        report.setStatus(0);
        reportMapper.insert(report);
    }

    @Override
    public List<ReportVO> list(Integer status) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .orderByDesc(Report::getCreatedAt)
                .orderByDesc(Report::getId);
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        List<Report> reports = reportMapper.selectList(wrapper);
        if (reports.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = reports.stream().map(Report::getUserId).distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        return reports.stream().map(r -> {
            ReportVO vo = new ReportVO();
            vo.setId(r.getId());
            vo.setVideoId(r.getVideoId());
            vo.setCommentId(r.getCommentId());
            vo.setUserId(r.getUserId());
            vo.setUserName(nameMap.get(r.getUserId()));
            vo.setReason(r.getReason());
            vo.setDescription(r.getDescription());
            vo.setStatus(r.getStatus());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, Integer status) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("举报不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException("非法的处置状态");
        }
        report.setStatus(status);
        reportMapper.updateById(report);
    }
}
