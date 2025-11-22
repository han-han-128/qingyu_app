package com.example.qing.service;

import com.example.qing.entity.DetectionRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * 检测记录服务接口
 */
public interface DetectionRecordService {
    /**
     * 保存检测记录
     * @param userId 用户ID
     * @param image 上传的图片文件
     * @return 检测记录对象
     * @throws Exception 处理过程中发生的异常
     */
    DetectionRecord saveDetectionRecord(String userId, MultipartFile image) throws Exception;
}