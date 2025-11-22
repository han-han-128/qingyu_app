package com.example.qing.service.impl;

import com.example.qing.entity.ReportRecord;
import com.example.qing.entity.ReportImage;
import com.example.qing.mapper.ReportRecordMapper;
import com.example.qing.mapper.ReportImageMapper;
import com.example.qing.service.ReportRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 紧急情况上报记录服务实现类
 */
@Service
public class ReportRecordServiceImpl implements ReportRecordService {

    @Autowired
    private ReportRecordMapper reportRecordMapper;

    @Autowired
    private ReportImageMapper reportImageMapper;

    /**
     * 上报紧急情况
     * 使用事务确保上报记录和图片信息要么都保存成功，要么都失败
     */
    @Override
    @Transactional
    public Long reportEmergency(ReportRecord reportRecord, List<String> imageUrls) throws Exception {
        // 1. 生成上报编号
        String reportNo = generateReportNo();
        reportRecord.setReportNo(reportNo);
        reportRecord.setReportTime(new Date());
        if (reportRecord.getStatus() == null) {
            reportRecord.setStatus(1); // 默认状态为待处理
        }

        // 2. 保存上报记录
        int recordResult = reportRecordMapper.insertReportRecord(reportRecord);
        if (recordResult <= 0) {
            throw new Exception("保存上报记录失败");
        }

        // 3. 保存图片信息（如果有图片）
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ReportImage reportImage = new ReportImage();
                reportImage.setReportId(reportRecord.getId());
                reportImage.setImageUrl(imageUrl);
                reportImage.setCreateTime(new Date());

                int imageResult = reportImageMapper.insertReportImage(reportImage);
                if (imageResult <= 0) {
                    throw new Exception("保存上报图片失败");
                }
            }
        }

        return reportRecord.getId();
    }

    /**
     * 根据ID查询上报记录
     */
    @Override
    public ReportRecord getReportRecordById(Long id) {
        return reportRecordMapper.selectReportRecordById(id);
    }

    /**
     * 根据用户ID查询上报记录
     */
    @Override
    public List<ReportRecord> getReportRecordsByUserId(String userId) {
        return reportRecordMapper.selectReportRecordsByUserId(userId);
    }

    /**
     * 更新上报记录状态
     */
    @Override
    public boolean updateReportStatus(Long id, Integer status) {
        int result = reportRecordMapper.updateReportRecordStatus(id, status);
        return result > 0;
    }

    /**
     * 查询所有上报记录
     */
    @Override
    public List<ReportRecord> getAllReportRecords() {
        return reportRecordMapper.selectAllReportRecords();
    }

    /**
     * 生成上报编号
     * 格式：REPORT_YYYYMMDD_HHMMSS_随机UUID
     */
    private String generateReportNo() {
        Date now = new Date();
        String timeStr = String.format("%tY%<tm%<td_%<tH%<tM%<tS", now);
        String uuidStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "REPORT_" + timeStr + "_" + uuidStr;
    }
}