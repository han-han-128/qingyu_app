package com.example.qing.service.impl;

import com.example.qing.entity.DetectionRecord;
import com.example.qing.mapper.DetectionRecordMapper;
import com.example.qing.service.DetectionRecordService;
import com.example.qing.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.qing.utils.YoloONNXUtil;


import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * 检测记录服务实现类
 */
@Service
public class DetectionRecordServiceImpl implements DetectionRecordService {

    @Autowired
    private DetectionRecordMapper detectionRecordMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 保存检测记录
     * 使用事务确保所有操作要么都成功，要么都失败
     */
    @Override
    @Transactional
    public DetectionRecord saveDetectionRecord(String userId, MultipartFile image) throws Exception {
        // 生成唯一ID
        String recordId = UUID.randomUUID().toString();

        // 1. 上传原始图片到OSS
        String originalFileName = image.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String objectName = "original/" + UUID.randomUUID() + extension;
        String imageUrl = aliOssUtil.upload(image.getBytes(), objectName);

        // 2. 调用YOLOv模型识别图片（还未实现）
        // 导入部分
        
        // 方法部分
        YoloONNXUtil.DetectionResult result = YoloONNXUtil.detect(image.getBytes());
        String detectionResult = result.getDetectionResult();
        Double confidence = result.getConfidence();
        byte[] detectedImageBytes = result.getDetectedImageBytes();

        // 3. 上传识别后的图片到OSS
        String detectedObjectName = "detected/" + UUID.randomUUID() + extension;
        String detectedImageUrl = aliOssUtil.upload(detectedImageBytes, detectedObjectName);

        // 4. 保存检测记录到数据库
        DetectionRecord detectionRecord = new DetectionRecord();
        detectionRecord.setId(recordId);
        detectionRecord.setUserId(userId);
        detectionRecord.setImageUrl(imageUrl);
        detectionRecord.setDetectedImageUrl(detectedImageUrl);
        detectionRecord.setDetectionResult(detectionResult);
        detectionRecord.setDetectionTime(new Date());
        detectionRecord.setConfidence(confidence);

        int result1 = detectionRecordMapper.insertDetectionRecord(detectionRecord);
        if (result1 <= 0) {
            throw new Exception("保存检测记录失败");
        }

        return detectionRecord;
    }
}