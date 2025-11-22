package com.example.qing.entity;

import lombok.Data;
import java.util.Date;

/**
 * 检测记录实体类
 * 对应数据库中的detection_records表
 */
@Data
public class DetectionRecord {
    /**
     * 记录ID
     */
    private String id;
    
    /**
     * 用户ID，关联user表
     */
    private String userId;
    
    /**
     * 原始图片URL
     */
    private String imageUrl;
    
    /**
     * 识别后图片URL
     */
    private String detectedImageUrl;
    
    /**
     * 识别结果
     */
    private String detectionResult;
    
    /**
     * 识别时间
     */
    private Date detectionTime;
    
    /**
     * 置信度
     */
    private Double confidence;
}