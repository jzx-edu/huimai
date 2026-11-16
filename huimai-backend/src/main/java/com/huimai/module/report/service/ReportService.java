package com.huimai.module.report.service;

import com.huimai.module.report.dto.ReportCreateDTO;
import com.huimai.module.report.vo.ReportVO;

import java.util.List;

/**
 * 举报服务。
 */
public interface ReportService {

    /** 提交举报 */
    void submit(ReportCreateDTO dto, Long userId);

    /** 举报列表（后台，可按状态过滤） */
    List<ReportVO> list(Integer status);

    /** 处置举报（1 已处理 2 已驳回） */
    void handle(Long id, Integer status);
}
