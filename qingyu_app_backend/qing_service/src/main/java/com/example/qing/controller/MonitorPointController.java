package com.example.qing.controller;

import com.example.qing.entity.MonitorPoint;
import com.example.qing.service.MonitorPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitors")
@CrossOrigin(origins = "*") // 允许跨域，小程序调用需要
public class MonitorPointController {

    @Autowired
    private MonitorPointService monitorPointService;

    // 根据地图类型获取监测点
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMonitorsByMapType(@RequestParam String mapType) {
        try {
            List<MonitorPoint> monitors = monitorPointService.getMonitorsByMapType(mapType);
            return ResponseEntity.ok(createSuccessResponse(monitors));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("获取监测点失败: " + e.getMessage()));
        }
    }

    // 根据ID获取监测点详情
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMonitorById(@PathVariable Long id) {
        try {
            MonitorPoint monitor = monitorPointService.getMonitorById(id);
            if (monitor != null) {
                return ResponseEntity.ok(createSuccessResponse(monitor));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("监测点不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("获取监测点详情失败: " + e.getMessage()));
        }
    }

    // 获取所有监测点
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMonitors() {
        try {
            List<MonitorPoint> monitors = monitorPointService.getAllMonitors();
            return ResponseEntity.ok(createSuccessResponse(monitors));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("获取监测点列表失败: " + e.getMessage()));
        }
    }

    // 新增监测点
    @PostMapping
    public ResponseEntity<Map<String, Object>> addMonitor(@RequestBody MonitorPoint monitorPoint) {
        try {
            boolean success = monitorPointService.addMonitor(monitorPoint);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("添加监测点成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("添加监测点失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("添加监测点失败: " + e.getMessage()));
        }
    }

    // 更新监测点
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMonitor(@PathVariable Long id, @RequestBody MonitorPoint monitorPoint) {
        try {
            monitorPoint.setId(id);
            boolean success = monitorPointService.updateMonitor(monitorPoint);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("更新监测点成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("更新监测点失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("更新监测点失败: " + e.getMessage()));
        }
    }

    // 删除监测点
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMonitor(@PathVariable Long id) {
        try {
            boolean success = monitorPointService.deleteMonitor(id);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除监测点成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除监测点失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("删除监测点失败: " + e.getMessage()));
        }
    }

    // 统一的成功响应格式
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    // 统一的错误响应格式
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}