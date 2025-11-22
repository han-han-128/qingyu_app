package com.example.qing.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorData {
    private Long id;
    private Long monitorId;   // 注意：这里我们使用monitorId来关联，而不是MonitorPoint对象
    private String dataType;
    private BigDecimal value;
    private LocalDateTime recordedTime;
}