package com.example.qing.entity;

import lombok.Data;
import java.util.Date;

/**
 * 紧急情况上报记录实体类
 * 对应数据库中的report_record表
 */
@Data
public class ReportRecord {
    /**
     * 上报记录ID
     */
    private Long id;
    
    /**
     * 上报编号
     */
    private String reportNo;
    
    /**
     * 用户ID，关联user表
     */
    private String userId;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 上报描述
     */
    private String description;
    
    /**
     * 紧急程度：1-一般，2-紧急，3-危急
     */
    private Integer emergencyLevel;
    
    /**
     * 状态：1-待处理，2-处理中，3-已处理，4-已关闭
     */
    private Integer status;
    
    /**
     * 上报时间
     */
    private Date reportTime;
    
    /**
     * 处理时间
     */
    private Date handleTime;
}