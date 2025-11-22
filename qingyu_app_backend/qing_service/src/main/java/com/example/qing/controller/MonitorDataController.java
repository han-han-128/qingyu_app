package com.example.qing.controller;

import com.example.qing.entity.MonitorData;
import com.example.qing.service.MonitorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitor-data")
@CrossOrigin(origins = "*")
public class MonitorDataController {

    @Autowired
    private MonitorDataService monitorDataService;

    // 获取监测点最新数据
    @GetMapping("/latest/{monitorId}")
    public ResponseEntity<Map<String, Object>> getLatestData(
            @PathVariable Long monitorId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<MonitorData> dataList = monitorDataService.getLatestData(monitorId, limit);
            return ResponseEntity.ok(createSuccessResponse(dataList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("获取监测数据失败: " + e.getMessage()));
        }
    }

    // 添加监测数据
    @PostMapping
    public ResponseEntity<Map<String, Object>> addMonitorData(@RequestBody MonitorData monitorData) {
        try {
            boolean success = monitorDataService.addMonitorData(monitorData);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("添加监测数据成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("添加监测数据失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("添加监测数据失败: " + e.getMessage()));
        }
    }

    // 批量添加监测数据
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchAddMonitorData(@RequestBody List<MonitorData> monitorDataList) {
        try {
            boolean success = monitorDataService.batchAddMonitorData(monitorDataList);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("批量添加监测数据成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("批量添加监测数据失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("批量添加监测数据失败: " + e.getMessage()));
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