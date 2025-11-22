package com.example.qing.entity;

import lombok.Data;
import java.util.Date;

/**
 * 紧急情况上报图片实体类
 * 对应数据库中的report_image表
 */
@Data
public class ReportImage {
    /**
     * 图片ID
     */
    private Long id;
    
    /**
     * 上报记录ID，关联report_record表
     */
    private Long reportId;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 创建时间
     */
    private Date createTime;
}