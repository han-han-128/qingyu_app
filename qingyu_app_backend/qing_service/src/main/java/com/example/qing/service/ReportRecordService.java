package com.example.qing.service;

import com.example.qing.entity.ReportRecord;
import com.example.qing.entity.ReportImage;
import java.util.List;

/**
 * 紧急情况上报记录服务接口
 */
public interface ReportRecordService {
    /**
     * 上报紧急情况
     * @param reportRecord 上报记录对象
     * @param imageUrls 图片URL列表
     * @return 上报记录ID
     * @throws Exception 处理过程中发生的异常
     */
    Long reportEmergency(ReportRecord reportRecord, List<String> imageUrls) throws Exception;

    /**
     * 根据ID查询上报记录
     * @param id 上报记录ID
     * @return 上报记录对象
     */
    ReportRecord getReportRecordById(Long id);

    /**
     * 根据用户ID查询上报记录
     * @param userId 用户ID
     * @return 上报记录列表
     */
    List<ReportRecord> getReportRecordsByUserId(String userId);

    /**
     * 更新上报记录状态
     * @param id 上报记录ID
     * @param status 新状态
     * @return 更新是否成功
     */
    boolean updateReportStatus(Long id, Integer status);

    /**
     * 查询所有上报记录
     * @return 上报记录列表
     */
    List<ReportRecord> getAllReportRecords();
}