package com.example.qing.service;

import com.example.qing.entity.MonitorPoint;

import java.util.List;

public interface MonitorPointService {

    List<MonitorPoint> getMonitorsByMapType(String mapType);

    MonitorPoint getMonitorById(Long id);

    List<MonitorPoint> getAllMonitors();

    boolean addMonitor(MonitorPoint monitorPoint);

    boolean updateMonitor(MonitorPoint monitorPoint);

    boolean deleteMonitor(Long id);

    boolean updateMonitorStatus(Long id, String status);
}