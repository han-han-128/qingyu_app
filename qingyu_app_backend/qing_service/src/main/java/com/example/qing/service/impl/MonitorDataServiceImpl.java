package com.example.qing.service.impl;

import com.example.qing.entity.MonitorData;
import com.example.qing.mapper.MonitorDataMapper;
import com.example.qing.service.MonitorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MonitorDataServiceImpl implements MonitorDataService {

    @Autowired
    private MonitorDataMapper monitorDataMapper;

    @Override
    public List<MonitorData> getLatestData(Long monitorId, int limit) {
        return monitorDataMapper.findByMonitorId(monitorId, limit);
    }

    @Override
    public boolean addMonitorData(MonitorData monitorData) {
        return monitorDataMapper.insert(monitorData) > 0;
    }

    @Override
    public boolean batchAddMonitorData(List<MonitorData> monitorDataList) {
        return monitorDataMapper.batchInsert(monitorDataList) > 0;
    }

    @Override
    public List<MonitorData> getDataByTypeAndTimeRange(Long monitorId, String dataType, String startTime, String endTime) {
        return monitorDataMapper.findByTypeAndTimeRange(monitorId, dataType, startTime, endTime);
    }
}