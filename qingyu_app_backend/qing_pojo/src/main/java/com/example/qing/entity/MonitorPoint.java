package com.example.qing.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorPoint {
    private Long id;
    private String name;
    private String mapType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status = "ONLINE";
    private String videoStreamUrl;
    private String rtspUrl;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}