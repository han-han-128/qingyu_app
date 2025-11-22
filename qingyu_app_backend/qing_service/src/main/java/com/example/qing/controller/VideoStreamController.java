package com.example.qing.controller;

import com.example.qing.entity.MonitorPoint;
import com.example.qing.service.MonitorPointService;
import com.example.qing.service.VideoStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "*")
public class VideoStreamController {

    @Autowired
    private VideoStreamService videoStreamService;

    @Autowired
    private MonitorPointService monitorPointService;

    @GetMapping("/start/{monitorId}")
    public ResponseEntity<Map<String, Object>> startStream(@PathVariable Long monitorId) {
        try {
            MonitorPoint monitor = monitorPointService.getMonitorById(monitorId);
            if (monitor == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("监测点不存在"));
            }

            String videoStreamUrl = monitor.getVideoStreamUrl();
            if (videoStreamUrl == null || videoStreamUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("监测点未配置视频流地址"));
            }

            String hlsUrl = videoStreamService.startStream(monitorId, videoStreamUrl);

            Map<String, Object> data = new HashMap<>();
            data.put("hlsUrl", hlsUrl);
            data.put("monitorId", monitorId);
            data.put("status", "streaming");

            return ResponseEntity.ok(createSuccessResponse(data));

        } catch (Exception e) {
            log.error("启动视频流失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("启动视频流失败: " + e.getMessage()));
        }
    }

    @GetMapping("/stop/{monitorId}")
    public ResponseEntity<Map<String, Object>> stopStream(@PathVariable Long monitorId) {
        try {
            videoStreamService.stopStream(monitorId);
            return ResponseEntity.ok(createSuccessResponse("视频流已停止"));
        } catch (Exception e) {
            log.error("停止视频流失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("停止视频流失败: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{monitorId}")
    public ResponseEntity<Map<String, Object>> getStreamStatus(@PathVariable Long monitorId) {
        try {
            boolean active = videoStreamService.isStreamActive(monitorId);
            Map<String, Object> data = new HashMap<>();
            data.put("monitorId", monitorId);
            data.put("active", active);
            if (active) {
                data.put("hlsUrl", videoStreamService.getHlsUrl(monitorId));
            }
            return ResponseEntity.ok(createSuccessResponse(data));
        } catch (Exception e) {
            log.error("获取流状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("获取流状态失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}