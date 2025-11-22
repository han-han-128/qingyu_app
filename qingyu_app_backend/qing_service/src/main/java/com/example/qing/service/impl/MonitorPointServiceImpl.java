package com.example.qing.service.impl;

import com.example.qing.entity.MonitorPoint;
import com.example.qing.mapper.MonitorPointMapper;
import com.example.qing.service.MonitorPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MonitorPointServiceImpl implements MonitorPointService {

    @Autowired
    private MonitorPointMapper monitorPointMapper;

    @Override
    public List<MonitorPoint> getMonitorsByMapType(String mapType) {
        return monitorPointMapper.findByMapType(mapType);
    }

    @Override
    public MonitorPoint getMonitorById(Long id) {
        return monitorPointMapper.findById(id);
    }

    @Override
    public List<MonitorPoint> getAllMonitors() {
        return monitorPointMapper.findAll();
    }

    @Override
    public boolean addMonitor(MonitorPoint monitorPoint) {
        // 设置默认状态
        if (monitorPoint.getStatus() == null) {
            monitorPoint.setStatus("ONLINE");
        }
        return monitorPointMapper.insert(monitorPoint) > 0;
    }

    @Override
    public boolean updateMonitor(MonitorPoint monitorPoint) {
        return monitorPointMapper.update(monitorPoint) > 0;
    }

    @Override
    public boolean deleteMonitor(Long id) {
        return monitorPointMapper.delete(id) > 0;
    }

    @Override
    public boolean updateMonitorStatus(Long id, String status) {
        return monitorPointMapper.updateStatus(id, status) > 0;
    }
}