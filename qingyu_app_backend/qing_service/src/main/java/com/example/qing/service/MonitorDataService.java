package com.example.qing.service;

import com.example.qing.entity.MonitorData;

import java.util.List;

public interface MonitorDataService {

    List<MonitorData> getLatestData(Long monitorId, int limit);

    boolean addMonitorData(MonitorData monitorData);

    boolean batchAddMonitorData(List<MonitorData> monitorDataList);

    List<MonitorData> getDataByTypeAndTimeRange(Long monitorId, String dataType, String startTime, String endTime);
}